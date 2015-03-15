(ns osm.writer-test
  (:require [clojure.java.io :as io])
  (:use [clojure.data.json :only [read-str write-str] ])
  (:use osm.writer)
  (:use midje.sweet))

(def demo (io/resource "demo.osm.bz2"))

(def data-dir (io/file "data"))
(if (not (.exists data-dir)) (.mkdir data-dir))

(fact "Spit it out"

  (spit-each demo (io/file data-dir))
  (:geometry (read-str (slurp (io/file data-dir "1012960250.geojson" )) :key-fn keyword)) => {:type "Point", :coordinates [-43.1931519 -22.9590546]}

  (spit-all demo (io/file "data" "all.geojson" ))
  (slurp (io/file data-dir "all.geojson")) => (slurp (io/resource "demo.geojson"))

  (spit-each-swap demo (io/file data-dir))
  (:geometry (read-str (slurp (io/file data-dir "1012960250.geojson" )) :key-fn keyword)) => {:type "Point", :coordinates [-43.1931519 -22.9590546]}

  (spit-all-swap  demo (io/file "data" "all.geojson"))
  (slurp (io/file data-dir "all.geojson")) => (slurp (io/resource "demo.geojson"))

)


