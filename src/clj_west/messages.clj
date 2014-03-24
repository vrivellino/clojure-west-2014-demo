(ns clj-west.messages
  "Transacting and querying messages"
  (:require [clojure.tools.logging :as log]
            [clojure.set :as set]
            [datomic.api :as d]))

(defn ent->message
  [ent]
  (when ent
    (-> (zipmap (keys ent) (vals ent))
        (assoc :clj-west/basis-t (-> ent d/entity-db d/basis-t))
        (dissoc :db/id))))

(defn message-txdata
  [eid k v]
  (if (= :id k)
    [[:db/add eid :clj-west.messages/id v]]
    [[:db/add eid :clj-west.messages/msg v]]))

(defn message->txdata
  [message]
  (let [eid (d/tempid :clj-west.part/messages)]
    (reduce-kv (fn [msg-txdata k v]
                 (into msg-txdata (message-txdata eid k v)))
               [] message)))



(defn transact
  [conn request]
  (let [message (select-keys (:params request) [:id :msg])
        txdata (message->txdata message)
        tx-future (d/transact-async conn txdata)
        timeout-duration 2000
        result (try
                 (deref tx-future timeout-duration ::timeout)
                 (catch clojure.lang.ExceptionInfo x x))]
    (cond
     (= ::timeout result)
     (throw (ex-info "Transaction timed out. Sorry Vince... I owe you a beer."
                     {:clj-west/reason :clj-west/transaction-timeout}))
     (instance? clojure.lang.ExceptionInfo result)
     (throw (ex-info "Something bad happened and I owe you at least 3 beers."
                     {:clj-west/reason :clj-west/bad-programmer}))
     :else
     (-> result :db-after d/basis-t))))

(defn query
  [db request]
  (when db
    (let [id (:id (:params request))]
      (->> (d/q '[:find ?e
                  :in $ ?attr ?id
                  :where
                  [?e ?attr ?id]]
                db
                :clj-west.messages/id
                id)
           ffirst
           (d/entity db)
           (ent->message)
           :clj-west.messages/msg))))
