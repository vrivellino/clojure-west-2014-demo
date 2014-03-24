(ns clj-west.server
  "App, routing, handlers"
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            ring.middleware.params
            ring.middleware.keyword-params
            [datomic.api :as d]
            [clj-west.messages :as m]))

(def ^:dynamic *app-context*
  nil)

(defn- datomic-conn
  []
  (d/connect (:uri (:ddb-peer *app-context*))))

(defn- write-endpoint
  [request]
  (str (m/transact (datomic-conn) request)))

(defn- read-endpoint
  [request]
  (let [db (d/db (datomic-conn))]
    (str (m/query db request))))

(defroutes ^:private app-routes
  (GET "/write" [] write-endpoint)
  (GET "/read" [] read-endpoint)
  (GET "/hc" [] "<h1>I'm Alive!</h1>"))

(defn- dynamic-app-context
  [handler options]
  (if (:dynamic? options)
    (fn [request]
      (binding [*app-context* options]
        (handler request)))
    handler))

(defn- wrap-db-uri
  [handler]
  (fn [request]
    (handler (assoc request :clj-west/ddb-uri (-> *app-context* :ddb-peer :uri)))))

(defn app
  [options]
  (-> app-routes
      ring.middleware.params/wrap-params
      ring.middleware.keyword-params/wrap-keyword-params
      wrap-db-uri
      (dynamic-app-context options)))
