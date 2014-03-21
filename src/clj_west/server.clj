(ns clj-west.server
  "App, routing, handlers"
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            ring.middleware.params
            [datomic.api :as d]
            [clj-west.messages :as m]))

(def ^:dynamic *app-context*
  nil)

(defn- datomic-conn
  []
  (d/connect (:uri (:ddb-peer *app-context*))))

(defn- write-endpoint
  [request]
  (log/info request)
  (str (m/transact (datomic-conn) request)))

(defn- read-endpoint
  [request]
  (let [db (d/db (datomic-conn))]
    (str (m/query db :clj-west.messages/id (:id (:params request))))))

(defroutes ^:private app-routes
  (GET "/write" request (write-endpoint request))
  (GET "/read" request (read-endpoint request)))

(defn- dynamic-app-context
  [handler options]
  (log/info "Dynamic App Context")
  (if (:dynamic? options)
    (fn [request]
      (binding [*app-context* options]
        (handler request)))
    handler))

(defn- wrap-db-uri
  [handler]
  (fn [request]
    (log/info "Wrap DB URI HANDLER")
    (handler (assoc request :clj-west/ddb-uri (-> *app-context* :ddb-peer :uri)))))

(defn app
  [options]
  (-> app-routes
      ring.middleware.params/wrap-params
      wrap-db-uri
      (dynamic-app-context options)))
