(defproject osm "0.0.5"
  :description "Working with OpenStreetMaps data"
  :license {:name "MIT"}
  :main osm.cli
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-http "3.8.0"]
                 [factual/clj-leveldb "0.1.1"]
                 [byte-streams "0.2.3"]
                 [batcher "0.1.1"]
                 [org.apache.commons/commons-compress "1.16.1"]]
  :repositories [["clojars" {:sign-releases false}]]
  :uberjar-name "osm.jar"
  :profiles {:uberjar {:aot :all}
             :jar {:aot :all}
             :dev {:dependencies [[midje "1.9.1"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring/ring-core "1.6.3"]
                                  [ring/ring-jetty-adapter "1.6.3"]]
                   :plugins [[lein-midje "3.2.1"]]}})
