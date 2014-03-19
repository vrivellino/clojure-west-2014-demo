(ns clj-west.server
  "App, routing, handlers"
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            ring.middleware.params
            ring.middleware.keyword-params
            [datomic.api :as d]))

(def ^:dynamic *app-context*
  nil)

(defn- datomic-conn
  []
  (d/connect (:uri (:ddb-peer *app-context*))))

(defn- write-endpoint
  [request]
  ;; TODO: Add actual handler transaction logic
  )

(defn- read-endpoint
  [request]
  ;; TODO: Add actual handler transaction logic
  )

(defroutes ^:private app-routes
  (GET "/write" request (write-endpoint request))
  (GET "/read" request (read-endpoint request)))

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
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      wrap-db-uri
      (dynamic-app-context options)))
