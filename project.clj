(defproject osm "0.0.1"
  :description "Working with OpenStreetMaps data"
  :license {:name "MIT"}
  :main osm.cli
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.371"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-http-lite "0.3.0"]
                 [factual/clj-leveldb "0.1.1"]
                 [byte-streams "0.1.13"]
                 [batcher "0.1.1"]
                 [org.apache.commons/commons-compress "1.9"]]
  :repositories [["clojars" {:sign-releases false}]]
  :profiles {:uberjar {:aot :all}
             :jar {:aot :all}
             :dev {:dependencies [[midje "1.8.2"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring/ring-core "1.4.0"]
                                  [ring/ring-jetty-adapter "1.4.0"]]
                   :plugins [[lein-midje "3.2"]]}})
