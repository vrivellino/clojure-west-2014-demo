(ns clj-west.datomic
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [clj-west.lifecycle :refer (Lifecycle)])
  (:import [clojure.lang ExceptionInfo]
           [java.util.concurrent Executors TimeUnit ScheduledExecutorService]
           [org.joda.time LocalTime]
           [java.security MessageDigest]))

(defn digest
  [s]
  (let [hash (MessageDigest/getInstance "SHA-256")]
    (.update hash (.getBytes s))
    (let [digest (.digest hash)]
      (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

(defn- schema-present?
  [db id]
  (and (d/entid db :clj-west.schema/transacted-schema-id)
       (seq (d/q '[:find ?e
                   :in $ ?id
                   :where [?e :clj-west.schema/transacted-schema-id ?id]]
                 db
                 id))))

(defn- transact-schema
  [conn schema schema-id inst]
  (let [inst-assertion {:db/txInstant inst
                        :db/id (d/tempid :db.part/tx)}]
    (doseq [tx schema]
      @(d/transact conn
                   (if inst (conj tx inst-assertion) tx)))
    (let [schema-assertion {:db/id (d/tempid :clj-west.part/schemas)
                            :clj-west.schema/transacted-schema-id schema-id}]
      @(d/transact conn
                   (if inst
                     [schema-assertion inst-assertion]
                     [schema-assertion])))))

(defn ensure-schema
  ([conn] (ensure-schema conn nil))
  ([conn inst]
     (let [schema-str (binding [*read-eval* false]
                        (slurp (io/resource "schema.edn")))
           schema-id (str (digest schema-str))]
       (if (schema-present? (d/db conn) schema-id)
         (log/info "Schema"
                   schema-id
                   "is already present in the database. Doing nothing.")
         (do
           (log/info "Asserting schema" schema-id)
           (transact-schema conn
                           (-> schema-str read-string)
                           schema-id
                           inst))))))

(defn init-db
  ([conn] (init-db conn nil))
  ([conn inst]
     (ensure-schema conn inst)))

(defrecord TemporaryPeer [uri]
  Lifecycle
  (start [_]
    (future
      (log/info :STARTING "temporary-peer" :uri uri)
      (d/create-database uri)
      (let [conn (d/connect uri)]
        (init-db conn))))
  (stop [_]
    (future
      (log/info :STOPPING "temporary-peer" :uri uri)
      (d/delete-database uri))))

(defn temp-peer
  []
  (let [name (d/squuid)
        uri (str "datomic:mem:" name)]
    (->TemporaryPeer uri)))

(defrecord PersistentPeer [uri
                           memcached-nodes]
  Lifecycle
  (start [_]
    (future
      (log/info :STARTING "persistent-peer" :uri uri :memcached-nodes memcached-nodes)
      (when-not (str/blank? memcached-nodes)
        (System/setProperty "datomic.memcacheServers" memcached-nodes))
      (try

        (let [conn (d/connect uri)]
          (init-db conn))
        (catch Throwable t
          (log/error t :STARTING "Failed to initialize database")
          (throw t)))))
  (stop [_]
    (log/info :STOPPING "persistent-peer" :uri uri)
    (future nil)))

(defn persistent-peer
  "Returns an object implementing the Lifecycle protocol for a
  persistent Datomic database using the given URI and memcached nodes
  (which may be blank). Ensures on startup that the database has been
  created and the schema has been asserted."
  ([uri]
     (persistent-peer uri ""))
  ([uri memcached-nodes]
     (->PersistentPeer uri
                       memcached-nodes)))
