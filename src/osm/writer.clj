(ns osm.writer
  (:use osm.reader)
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defn spit-each
  "Spit(write) a feature to a dir"
  ([file dest] (spit-each file dest false))
  ([file dest swap]
   (let [dir (io/file dest)]
     (if (not (.exists dir)) (.mkdir dir))
      ((if swap read-stream-swap read-stream) file
        (fn [feature]
         (spit 
           (io/file dir (str (get-in feature [:properties :id]) ".geojson"))
           (json/write-str feature)))))))

(defn spit-each-swap
  "Spit(write) a feature to a dir, swapping"
  [file dest] (spit-each file dest true))

(defn spit-all
  "Spit whole XML as a FeatureCollection"
  ([file dest] (spit-all file dest false))
  ([file dest swap]
    (with-open [writer (io/writer (io/file dest))]
      (let [first? (atom true)]
        (.write writer "{\"type\":\"FeatureCollection\",\"features\":[")
        ((if swap read-stream-swap read-stream) file
          (fn [feature]
            (if @first?
              (do (swap! first? (fn [a] false))
                  (.write writer (json/write-str feature)))
              (.write writer (str "," (json/write-str feature) "\n")))))
        (.write writer "]}")))))

(defn spit-all-swap
  "Spit whole XML as a FeatureCollection, swapping to disk"
  [file dest] (spit-all file dest true))
