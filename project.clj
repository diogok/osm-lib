(defproject osm "0.0.1"
  :description "Working with OpenStreetMaps data"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.json "0.2.5"]
                 [factual/riffle "0.1.3-SNAPSHOT"]
                 [factual/clj-leveldb "0.1.1"]
                 [byte-streams "0.1.13"]
                 [org.apache.commons/commons-compress "1.9"]]
  :repositories [["clojars" {:sign-releases false}]]
  :profiles {:uberjar {:aot :all}
             :jar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
