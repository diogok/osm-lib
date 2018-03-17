(ns osm.cli-test
  (:require [clojure.java.io :as io])
  (:use osm.cli)
  (:use midje.sweet))

(def data-dir-0 (io/file "data"))
(if (not (.exists data-dir-0)) (.mkdir data-dir-0))
(def data-dir (io/file data-dir-0 "tmp"))
(if (not (.exists data-dir)) (.mkdir data-dir))

(fact "CLI interface work"
  (-main)
  (-main "-h")
  (-main "-i")
  (-main "-i" "not")
  (-main "-o" "data/tmp/demo.geo.json")
  (-main "-i" "test/data/demo.osm.bz2" "-o" "data/tmp/cli.geo.json")
  (-main "-i" "test/data/demo.osm.bz2" "-o" "data/tmp/cli.ff.geo.json" "-f" "amenity=fast_food")
  #_(-main "-i" "test/data/demo.osm.bz2") 
      )
