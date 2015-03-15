(ns osm.core
  (:require [riffle.write :as w] [riffle.read :as r])
  (:require [clj-leveldb :as l] [byte-streams :as bs])
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.data.json :as json]))

(defn bz2
  "Returns a streaming Reader for a compressed bzip2 file."
  [file]
    (org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream. (io/input-stream file) true))

(defn open-xml
  "Open the XML."
  [file-in]
   (let [file (io/file file-in)]
     (rest 
       (:content 
         (xml/parse
           (io/reader
             (if (.endsWith (.getName file) ".bz2")
               (bz2 file)
               (io/input-stream file))))))))

(defn get-props
  [el]
    (merge (:attrs el)
      (reduce merge
         (map 
           #(hash-map (keyword (:k (:attrs % ))) (:v (:attrs % ))) 
             (filter
               #(= (:tag %) :tag)
                (:content el))))))

(defn make-node
  "Transform node element in nice hash-map"
  [node]
   {
    :type "Feature"
    :properties (get-props node)
    :geometry {
      :type "Point"
      :coordinates [(Double/parseDouble (:lon (:attrs node))) (Double/parseDouble (:lat (:attrs node)))]
    }
   })

(defn make-node-hash-from-tag
  "Make a hashmap with node id and coords, to use with the ways"
  [node]
  {(:id (:attrs node))
    [(Double/parseDouble (:lon (:attrs node))) 
     (Double/parseDouble (:lat (:attrs node)))]})

(defn get-nodes-for-point
  "Lazy sequence of nodes"
  [xml] 
   (map make-node 
    (filter 
      (fn [n] (>= (count (:content n)) 1))
      (take-while #(= (:tag %) :node) xml))))

(defn get-nodes-hash
  "Merge nodes hashs, to use with ways"
  [xml]
   (reduce merge
     (map make-node-hash-from-tag 
        (take-while #(= (:tag %) :node) xml))))

(defn get-way-refs
  "Reffs of a way"
  [way] (map #(get-in % [:attrs :ref]) (filter #(= (:tag %) :nd) (:content way))))

(defn make-way-0
  "Raw Way geojson object"
  [props nodes]
  (let [gtype   (if (= (first nodes) (last nodes)) "Polygon" "LineString")]
    {
     :type "Feature"
     :properties props
     :geometry {
      :type gtype
      :coordinates (if (= gtype "Polygon") [nodes] nodes)}}))

(defn make-way
  "Transform a way element in a nice hash-map"
  [nodes way]
  (let [refs    (get-way-refs way)
        nodes   (map #(get nodes %) refs)]
    (make-way-0 (get-props way) nodes)))

(defn get-ways
  "Lazy sequence of ways"
  [nodes xml]
   (map 
     (partial make-way nodes) 
     (take-while 
       #(= (:tag %) :way) 
       (drop (count nodes) xml))))

(defn read-stream
  "Convert from OSM xml to GEOJson"
  [file fun] 
  (let [xml   (open-xml file)]
    (doseq [node (get-nodes-for-point xml)]
      (fun node))
    (doseq [way (get-ways (get-nodes-hash xml) xml)]
      (fun way))))

(defn read-all
  "Read all OSM xml into a vector. Not recomended for big files"
  [file] 
  (let [all (transient [])]
    (read-stream file
      #(conj! all %))
    (persistent! all)))

(defn node-to-file
  "A tree like structure to a file id"
  [id] 
   (let [parts (partition-all 2 id)]
     (io/file (apply str (first parts))
      (io/file (apply str (second parts))
         (str (apply str (flatten (drop 2 parts))) ".tmp")))))

(defn swap-in
  "Read a swapped node"
  [dir id] 
  (mapv
    #(Double/parseDouble %)
    (.split 
      (slurp (io/file dir (node-to-file id)))
      ",")))

(defn swap-out
  "Write a node to swap"
  [dir id data] 
  (spit 
    (io/file dir (node-to-file id))
    (str (first data) "," (second data))
    )) 

(defn mk-dir
  "Give a writable dir to swap"
  #_"TODO: random name"
  [] (io/file (io/file (System/getProperty "java.io.tmpdir") "osmer") "test1"))

(defn delete-dir
  "Recur delete a dir"
  [dir] nil)

(defn make-way-swap
  "Make way, using a swap"
  [dir way]
  (let [refs    (get-way-refs way)
        nodes   (mapv #(swap-in dir %) refs)]
    (make-way-0 (get-props way) nodes)))

(defn read-stream-swap
  "Read a OSM xml, swapping to disk. Use for bigger files."
  [file fun] 
  (let [dir      (mk-dir)
        xml      (open-xml file)]
    (doseq [node xml]
      (if (= (:tag node) :node) 
        (do
          (if (>= (count (:content node)) 1)
            (fun (make-node node)))
          (swap-out dir (:id (:attrs node)) [(:lon (:attrs node)) (:lat (:attrs node))]))
        (if (= (:tag node) :way)
          (fun (make-way-swap dir node)))))
    (delete-dir dir)
    ))

(defn spitter
  "Spit(write) a feature to a dir"
  [dir geojson]
   (spit 
     (io/file dir (str (get-in geojson [:properties :id]) ".geojson"))
     (json/write-str geojson)))

(defn spit-all
  "Spit whole XML as a FeatureCollection"
  [file dest swap]
  (with-open [writer (io/writer (io/file dest))]
    (let [first? (atom true)]
      (.write writer "{\"type\":\"FeatureCollection\",\"features\":[")
      ((if swap read-stream-swap read-stream) file
        (fn [feature]
          (if @first?
            (do (swap! first? (fn [a] false))
                (.write writer (json/write-str feature)))
            (.write writer (str "," (json/write-str feature) "\n")))))
      (.write writer "]}"))))

(defn query
  ""
  [ & points ] nil)

(defn changes
  ""
  [since] nil)

