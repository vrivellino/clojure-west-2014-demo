(ns clj-west.config
  "Services config"
  (:require [clojure.tools.logging :as log]))

(defprotocol ValueProvider
  (value [this] "Returns the value"))

(defn- get-value
  [x]
  (if (satisfies? ValueProvider x)
    (value x)
    x))

(defrecord SystemPropertyValue [property fallback]
  ValueProvider
  (value [this]
    (if-let [v (System/getProperty property)]
      v
      (get-value fallback))))

(def ^:private datomic-config
  {:datomic-uri (->SystemPropertyValue "DATOMIC_URI"
                                       "datomic:dev://localhost:4334/clj-west")})

(defn get-datomic-config
  [id]
  (get-value (id datomic-config)))
