(ns osm.cli
  (:use osm.reader osm.writer)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :refer [file]])
  (:gen-class))

(def options
  [["-h" "--help"]
   ["-i" "--input INPUT" "Input OSM file, either .xml or .xml.bz2"
    :parse-fn file
    :validate [#(.exists %) "Input file not found"]]
   ["-o" "--output OUTPUT" "Output GeoJSON file, or URL to POST. Default to stdout."
    :default *out*
    :validate [#(not (.exists (file %))) "Output file already exists"]]
   ["-c" "--count NUMBER" "Number of features by POST, in case of URL output."
    :default 512
    :parse-fn #(Integer/valueOf %)
    :validate [#(>= % 0) "Only positive values for count."]]
   ["-s" "--swap" "If is to use swap file. Better for big datasets."
    :default true
    :parse-fn #(not (or (= "false") (= "no")))]])

(defn process
  [input output limit swap]
  (if (.startsWith output "http//")
    (post input output limit swap)
    (spit-all input output swap)))

(defn -main
  [ & args ]
  (let [opts (parse-opts args options)]
    (println "OpenStreetMaps to GeoJSON tool.")
    (cond
      (:help (:options opts)) 
        (println (:summary opts))
      (:errors opts) 
        (doseq [err (:errors opts)]
          (println err))
      (and (not (nil? (:input (:options opts))))
           (not (nil? (:output (:options opts))))) 
        (process (:input (:options opts)) 
                 (:output (:options opts)) 
                 (:count (:options opts))
                 (:swap (:options opts))))))

