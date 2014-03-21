(defproject clj-west "0.0.1-SNAPSHOT"
  :description "A barebones WS wrapper for Datomic"
  :url "http://github.com/vrivellino/clojure-west-demo"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.apache.httpcomponents/httpclient "4.3.1"
                  :exclusions [commons-logging]]
                 [ring/ring-core "1.2.0"]
                 [compojure "1.1.5"]
                 [com.datomic/datomic-pro "0.9.4609"]]
  :plugins [[lein-ring "0.8.0"]]
  ;; To access Datomic's maven repo, set the following environment variables:
  ;; export MY_DATOMIC_USERNAME=<USERNAME>
  ;; export MY_DATOMIC_PASSWORD=XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username :env/MY_DATOMIC_USERNAME
                                   :password :env/MY_DATOMIC_PASSWORD}}
  :ring {:handler clj-west.system/servlet-handler
         :init clj-west.system/init-servlet
         :destroy clj-west.system/destroy-servlet
         ;; Ring reloading conflicts with tools.namespace:
         :auto-reload? false
         :auto-refresh false
         :port 3001})
