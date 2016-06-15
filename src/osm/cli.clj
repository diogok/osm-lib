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
   ["-f" "--filter field=value" "Exact filter on data to output"
    :default nil]
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

(defn f2f
  [f] 
  (when-not (nil? f)
    (reduce
      (fn [h p] 
        (merge h {( keyword (first p) ) (second p)}))
      {}
      (map seq
        (map 
          #(.split % "=") 
          (.split f "&"))))))

(defn process
  [input output limit swap f]
  (println (f2f f))
  (if (.startsWith output "http//")
    (post input output limit swap (f2f f))
    (spit-all input output swap (f2f f))))

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
                 (:swap (:options opts))
                 (:filter (:options opts))))))

