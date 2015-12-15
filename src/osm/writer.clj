(ns osm.writer
  (:use osm.reader)
  (:use batcher.core)
  (:require [clojure.core.async :refer [<! <!! >! >!! chan close! go-loop]])
  (:require [clj-http.lite.client :as http])
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
    (with-open [writer (io/writer dest)]
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

(defn post-0
  [url data]
  (http/post url
    {:content-type :json
     :conn-timeout 15000
     :socket-timeout 15000
     :body (json/write-str {:type "FeatureCollection" :features data})}))

(defn post
  ([file url] (post file url 512 true))
  ([file url limit] (post file url limit true))
  ([file url limit swap] 
   (let [bat (batcher {:size limit :fn (partial post-0 url)})]
     ((if swap read-stream-swap read-stream) 
      file
      (partial >!! bat))
     (Thread/sleep 1000)
     (close! bat))))

