(defproject vriv-clj-west "0.0.1-SNAPSHOT"
  :description "A barebones WS wrapper for Datomic"
  :url "http://github.com/vrivellino/clojure-west-demo"
  :dependencies [[com.amazonaws/aws-java-sdk "1.6.1"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.apache.httpcomponents/httpclient "4.3.1"
                  :exclusions [commons-logging]]
                 [ring/ring-core "1.2.0"]
                 [compojure "1.1.5"]
                 [com.datomic/datomic-pro "starter.4202"]]
  :plugins [[lein-ring "0.8.0"]])
