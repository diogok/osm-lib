(use 'osm.reader)

(println "Starting")

(time
  (let [brazil (clojure.java.io/file "data" "brazil-latest.osm.bz2")
        counter (atom 0)
        last-node (atom nil)]
    (read-stream-swap brazil
      (fn [n] (do 
                  #_(println @counter ":" (:id (:properties n)) "->" (get-in n [:geometry :coordinates]))
                  (if (= 0 (mod @counter 1000)) (println "+1000"))
                  (swap! counter inc) 
                  (swap! last-node (fn[a] n)))))
    (println @counter)))

