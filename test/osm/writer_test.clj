(ns osm.writer-test
  (:require [clojure.java.io :as io])
  (:require [ring.adapter.jetty :refer [run-jetty]])
  (:use [clojure.data.json :only [read-str write-str read]])
  (:use osm.writer)
  (:use midje.sweet))

(def demo (io/file "test-data" "demo.osm.bz2"))

(def data-dir (io/file "data" "tmp"))
(if (not (.exists data-dir)) (.mkdir data-dir))

(fact "Matcher works"
  (let [match (matcher {:foo "bar"})]
    (match {:properties {:foo "bar"} }) => true
    (match {:foo "baz"}) => false
    ))

(fact "Spit it out"

  (spit-each demo (io/file data-dir))
  (:geometry (read-str (slurp (io/file data-dir "2217942280.geojson")) :key-fn keyword)) => {:type "Point", :coordinates [-43.1973948 -22.954548]}

  (spit-all demo (io/file "data" "tmp" "all.geojson" ))
  (slurp (io/file data-dir "all.geojson")) => (slurp (io/file "test-data" "demo.geojson"))

  (spit-each-swap demo (io/file data-dir))
  (:geometry (read-str (slurp (io/file data-dir "2217942280.geojson")) :key-fn keyword)) => {:type "Point", :coordinates [-43.1973948 -22.954548]}

  (spit-all-swap  demo (io/file "data" "tmp" "all.geojson"))
  (slurp (io/file data-dir "all.geojson")) => (slurp (io/file "test-data" "demo.geojson"))

  (spit-all-swap  demo (io/file "data" "tmp" "all2.geojson") {:amenity "fast_food"})
  (count (:features (read-str (slurp (io/file data-dir "all2.geojson")) :key-fn keyword))) =>  4
      )

(fact "Can post to web"
  (let [acc (atom 0)
        handler (fn [req] (swap! acc + (count (get (read (io/reader (:body req))) "features"))) "Ok.")
        server  (run-jetty handler {:port 3333 :join? false})]
    (post demo "http://localhost:3333" 10)
    (Thread/sleep 1000)
    (.stop server)
    @acc => 121
    ))


(fact "Can post to web with swap and filter"
  (let [acc (atom 0)
        handler (fn [req] (swap! acc + (count (get (read (io/reader (:body req))) "features"))) "Ok.")
        server  (run-jetty handler {:port 3333 :join? false})]
    (post demo "http://localhost:3333" 10 true {:amenity "fast_food"})
    (Thread/sleep 1000)
    (.stop server)
    @acc => 4
    ))
