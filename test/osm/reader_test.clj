(ns osm.reader-test
  (:require [clojure.java.io :as io])
  (:use [clojure.data.json :only [read-str write-str] ])
  (:use osm.reader)
  (:use midje.sweet))

(def demo (io/resource "demo.osm"))
(def demo-bz2 (io/resource "demo.osm.bz2"))

(fact "Can open XML"
  (count (open-xml demo)) => 843
  (count (open-xml demo-bz2)) => 843)

(fact "Can get the nodes"
  (let [xml    (open-xml demo)
        nodes0 (get-nodes-hash xml)
        nodes1 (get-nodes-for-point xml)
        ways   (get-ways (get-nodes-hash xml) xml)]
    (get nodes0 "142776489") => [-43.1941814 -22.9511706] 
    (count nodes1) => 57
    (first nodes1) => 
    {:type "Feature"
     :id "344369368"
     :properties {:amenity "fast_food", :changeset "13546858", :cuisine "pizza", :id "344369368", :lat "-22.9577648", :lon "-43.1994214", :name "Pizzaria Al Capone", :timestamp "2012-10-18T16:51:34Z", :uid "12293", :user "Nighto", :version "3", :visible "true"}
     :geometry {:coordinates [-43.1994214 -22.9577648] :type "Point"}}
    (count ways) => 64
    (first ways) => 
     {:geometry {:coordinates [[-43.1985341 -22.9586622] [-43.1986059 -22.9585118]  [-43.1987202 -22.9582615] [-43.1989132 -22.9578475] [-43.1989897 -22.9577656] [-43.1990344 -22.9577343] [-43.1990927 -22.9576856] [-43.1992169 -22.9576012]] :type "LineString"} :properties {:changeset "15473639" :highway "tertiary" :id "14521489" :name "Rua Macedo Sobrinho" :oneway "yes" :timestamp "2013-03-24T05:05:12Z" :uid "481662" :user "Geaquinto" :version "11" :visible "true"} :type "Feature" :id "14521489"}
    ))

(fact "Can read stream"
 (let [r (transient [])]
  (read-stream demo #(conj! r %))
   (let [r (persistent! r)]
    (count r) => 121
    (:id (:properties (last r))) => "319940648")))

(fact "Can read all"
  (:id (:properties (last (read-all demo)))) => "319940648")

(fact "Can read swapping"
 (let [r (transient [])]
  (read-stream-swap demo #(conj! r %))
   (let [r (persistent! r)]
    (count r) => 121
    (:id (:properties (last r))) => "319940648"
    (:coordinates (:geometry (last r))) => [[[-43.1938032 -22.9574944] [-43.1937427 -22.9574851] [-43.1937347 -22.957529] [-43.1937132 -22.9575257] [-43.1937028 -22.957583] [-43.1937271 -22.9575868] [-43.1937067 -22.9576995] [-43.1937269 -22.9577025] [-43.193721 -22.957735] [-43.1937679 -22.9577422] [-43.1937931 -22.9576029] [-43.1938136 -22.957606] [-43.1938283 -22.9575251] [-43.1937985 -22.9575205] [-43.1938032 -22.9574944]]]
     )))


