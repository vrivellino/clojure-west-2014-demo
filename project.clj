(defproject clj-west "0.0.1-SNAPSHOT"
  :description "A barebones WS wrapper for Datomic"
  :url "http://github.com/vrivellino/clojure-west-demo"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.httpcomponents/httpclient "4.3.5"
                  :exclusions [commons-logging]]
                 [ring/ring-core "1.3.1"]
                 [compojure "1.2.0"]
                 [com.datomic/datomic-pro "0.9.4956"
                  :exclusions [joda-time]]]
  :plugins [[lein-ring "0.8.12"]]
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
