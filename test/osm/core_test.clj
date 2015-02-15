(ns osm.core-test
  (:use [clojure.data.json :only [read-str write-str] ])
  (:use osm.core)
  (:use midje.sweet))

(def demo (clojure.java.io/resource "demo.osm"))

(fact "Can open XML"
  (count (open-xml demo)) => 843)

(fact "Can get the nodes"
  (let [xml   (open-xml demo)
        nodes (get-nodes xml)
        ways  (get-ways (get-nodes-compact xml) xml)]
    (first nodes) => 
    {:type "Feature"
     :properties {:changeset "4897635" :id "142776489" :lat "-22.9511706" :lon "-43.1941814" :timestamp "2010-06-04T02:41:28Z" :uid "289524" :user "Import Rio" :version "19" :visible "true"} 
     :geometry {:coordinates [-43.1941814 -22.9511706] :type "Point"}}
    (count ways) => 64
    (first ways) => 
     {:geometry {:coordinates [[-43.1985341 -22.9586622] [-43.1986059 -22.9585118]  [-43.1987202 -22.9582615] [-43.1989132 -22.9578475] [-43.1989897 -22.9577656] [-43.1990344 -22.9577343] [-43.1990927 -22.9576856] [-43.1992169 -22.9576012]] :type "LineString"} :properties {:changeset "15473639" :highway "tertiary" :id "14521489" :name "Rua Macedo Sobrinho" :oneway "yes" :timestamp "2013-03-24T05:05:12Z" :uid "481662" :user "Geaquinto" :version "11" :visible "true"} :type "Feature"}
    ))

(fact "Spit it out"
  (osm-stream demo (partial spitter "data"))
  (spit-all demo "data/all.geojson" false)
      )
