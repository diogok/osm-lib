(use 'osm.core)

(time
  (let [brazil (clojure.java.io/file "data" "brazil-latest.osm.bz2")
        counter (atom 0)
        last-node (atom nil)]
    (read-stream-swap brazil
      (fn [n] (do (println @counter ":" (:id (:properties n)))
                  (swap! counter inc) 
                  (swap! last-node (fn[a] n)))))
    (assert (= counter 1000))
    (assert (= (:id (:properties @last-node)) "123"))))

