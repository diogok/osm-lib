(ns osm.reader
  (:require [clojure.core.async :refer [<! <!! >! >!! chan close! go-loop]])
  (:require [clj-leveldb :as l] [byte-streams :as bs])
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]))

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
   {:type "Feature"
    :properties (get-props node)
    :id (:id (:attrs node))
    :geometry {
      :type "Point"
      :coordinates [(Double/parseDouble (:lon (:attrs node))) (Double/parseDouble (:lat (:attrs node)))]}})

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
     :id (:id props)
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
      (l/get dir id)
      ",")))

(defn swap-out
  "Write a node to swap"
  [dir id data] 
  (l/put dir id (str (first data) "," (second data)))) 

(defn mk-dir
  "Give a writable dir to swap"
  [] 
  (let [tmp (io/file (System/getProperty "java.io.tmpdir") "osmer")]
    (if (not (.exists tmp)) (.mkdir tmp))
    (io/file tmp (str (java.util.UUID/randomUUID) ".ldb"))))

(defn delete-dir
  "Recur delete a dir"
  [dir db] (.close db) (l/destroy-db dir))

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
        db       (l/create-db dir {:key-decoder byte-streams/to-string :val-decoder byte-streams/to-string :blocksize (* 1024 1024)})
        xml      (open-xml file)
        swap     (chan 2)
        out      (chan 2)
        done     (chan 1)]
    (go-loop [node (<! swap)]
      (if (not (nil? node))
        (do
          (swap-out db (:id (:attrs node)) [(:lon (:attrs node)) (:lat (:attrs node))])
          (recur (<! swap)))))
    (go-loop [node (<! out)]
      (if (nil? node)
        (close! done)
        (do 
          (if (= (:tag node) :node)
            (fun (make-node node))
            (fun (make-way-swap db node)))
          (recur (<! out)))))
    (doseq [node xml]
      (try
        (condp = (:tag node)
          :node 
            (do
              (>!! swap node)
              (if (>= (count (:content node)) 1)
                (>!! out node)))
          :way
            (>!! out node)
           nil)
      (catch Exception ex (do (binding [*out* *err*] (do (println node) (.printStackTrace ex)))))))
    (close! swap)
    (close! out)
    (<!! done)
    (delete-dir dir db)))

(defn changes
  ""
  [since] nil)

