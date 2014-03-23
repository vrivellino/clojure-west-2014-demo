(ns clj-west.system
  "Initialization"
  (:require [clj-west.config :as config]
            [clj-west.datomic]
            [clj-west.lifecycle :refer (Lifecycle start stop)]
            [clj-west.server]))

(def servlet-handler
  (clj-west.server/app {}))

(defn init-servlet
  []
  (let [ddb-peer (clj-west.datomic/persistent-peer (clj-west.config/get-datomic-config :datomic-uri))]
    @(start ddb-peer)
    (alter-var-root #'clj-west.server/*app-context*
                    (constantly {:ddb-peer ddb-peer}))))

(defn destroy-servlet
  []
  (let [ddb-peer (:ddb-peer clj-west.server/*app-context*)]
    (when ddb-peer @(stop ddb-peer))
    (alter-var-root #'clj-west.server/*app-context*
                    (constantly nil))))
