(ns clj-west.config
  "Services config"
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clj-west.lifecycle]))

(def ^:private datomic-config
  {:datomic-uri (->SystemPropertyValue "DATOMIC_URI")})

(defn get-datomic-config
  []
  datomic-config)
