(ns osm.core
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.data.json :as json]))
(defn query
  ""
  [ & points ] nil)

(defn open-xml
  "Open the XML."
  [file]
   (rest (:content (xml/parse (io/reader (io/file file))))))

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

(defn make-node-compact
  ""
  [node]
  (double-array
    (mapv #(Double/parseDouble %)
      [(:id (:attrs node)) (:lon (:attrs node)) (:lat (:attrs node))])))

(defn make-node-compact-1
  ""
  [node]
  (double-array
    [(Double/parseDouble (:id (:properties node)) )
     (first (:coordinates (:geometry node)))
     (second (:coordinates (:geometry node)))]))

(defn get-nodes
  "Lazy sequence of nodes"
  [xml] 
   (map make-node (take-while #(= (:tag %) :node) xml)))

(defn get-nodes-compact
  "Array of int array."
  [xml]
   (into-array (map make-node-compact (take-while #(= (:tag %) :node) xml))))

(defn make-way
  "Transform a way element in a nice hash-map"
  [nodes way]
  (let [refs    (map #(Double/parseDouble (get-in % [:attrs :ref])) (filter #(= (:tag %) :nd) (:content way)))
        refset  (set refs)
        nodeset (filter #(not (nil? (refset (first %)))) nodes)
        nodes   (mapv (fn [r] (first (filter #(= (first %) r) nodeset))) refs)
        gtype   (if (= (first refs) (last refs)) "Polygon" "LineString")
        coords  (mapv rest nodes)]
    {
     :type "Feature"
     :properties (get-props way)
     :geometry {
      :type gtype
      :coordinates (if (= gtype "Polygon") [coords] coords)
      }
     }
    ))

(defn get-ways
  "Lazy sequence of ways"
  [nodes xml]
   (map (partial make-way nodes) (take-while #(= (:tag %) :way) (drop (count nodes) xml))))

(defn osm-stream-swap
  ""
  [file fun] nil)

(defn osm-stream
  "Convert from OSM xml to GEOJson"
  [file fun] 
  #_"TODO: I should swap out to disk"
  (let [xml   (open-xml file)
        nodes (transient [])]
    (doseq [node (get-nodes xml)]
      (do
        (fun node)
        (conj! nodes (make-node-compact-1 node))
      ))
    (doseq [way (get-ways (persistent! nodes) xml)]
      (fun way))
    ))

(defn spitter
  "Spit(write) a feature to a file"
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
      ((if swap osm-stream-swap osm-stream) file
        (fn [feature]
          (if @first?
            (do (swap! first? (fn [a] false))
                (.write writer (json/write-str feature)))
            (.write writer (str "," (json/write-str feature) "\n")))))
      (.write writer "]}")
      )
    )
  )

