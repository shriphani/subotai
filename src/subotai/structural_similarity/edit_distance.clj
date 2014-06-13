(ns subotai.structural-similarity.edit-distance
  "Similarity score computed using the Tree Edit Distance algorithm"
  (:require [net.cgrand.enlive-html :as html]))

(def *sim-thresh* 0.71)

(defn html->map
  [page-src]
  (->> page-src
       java.io.StringReader.
       html/html-resource
       (filter (fn [x] (:tag x)))
       first))

(defn init
  [m n c1 c2 del-cost ins-cost]
  (let [M (make-array Integer/TYPE (inc m) (inc n))]
    (do
      (doseq [i (range (inc m))
              j (range (inc n))]
        (aset M i j (int
                     (+ (* del-cost c1)
                        (* ins-cost c2)))))
      M)))

(defn tree-children
  [a-tree]
  (->> a-tree :content (filter map?)))

(defn num-children
  [a-tree]
  (-> a-tree tree-children count))

(defn tree-descendants
  [a-tree]
  (if (-> a-tree tree-children seq)
    (+ (num-children a-tree)
       (apply + (map tree-descendants (tree-children a-tree))))
    0))

(declare tree-edit-distance)

(defn invert-cost
  [tree1 tree2 del-cost ins-cost sub-cost]
  (let [t1-desc (tree-descendants tree1)
        t2-desc (tree-descendants tree2)]
    (- (+ (* del-cost t1-desc)
          (* ins-cost t2-desc))
       (tree-edit-distance tree1 tree2 del-cost ins-cost sub-cost))))

(defn tree-edit-distance
  [tree1 tree2 del-cost ins-cost sub-cost]
  (let [m (num-children tree1)
        n (num-children tree2)

        t1-children (tree-children tree1)
        t2-children (tree-children tree2)

        t1-desc (tree-descendants tree1)
        t2-desc (tree-descendants tree2)

        M (init m n t1-desc t2-desc del-cost ins-cost)]
    
    (doseq [i (range m)
            j (range n)] 
      (let [c-i (nth t1-children i)
            c-j (nth t2-children j)

            c-i-desc (tree-descendants c-i)
            c-j-desc (tree-descendants c-j)

            del (aget M i (inc j))
            ins (aget M (inc i) j)

            sub-i (- (aget M i j)
                     del-cost
                     ins-cost)

            sub (if (= c-i c-j)
                  (- sub-i
                     (* ins-cost c-j-desc)
                     (* del-cost c-i-desc))
                  (cond
                   (or
                    (not (-> c-i :content (filter map?)))
                    (not (-> c-j :content (filter map?))))
                   (+ sub-i sub-cost)

                   (or (= (-> c-i :tag) (-> c-j :tag)))
                   (- sub-i (invert-cost c-i c-j del-cost ins-cost sub-cost))

                   :else
                   (+ sub-i sub-cost)))]
        (aset M (inc i) (inc j) (int (min del ins sub)))))
    (aget M m n)))

(defn similarity

  ([doc1 doc2]
     (similarity doc1 doc2 1 1 1))
  
  ([doc1 doc2 del-cost ins-cost sub-cost]
     (let [map1 (html->map doc1)
           map2 (html->map doc2)

           similarity-score (tree-edit-distance map1
                                                map2
                                                del-cost
                                                ins-cost
                                                sub-cost)
           
           d1 (tree-descendants map1)
           d2 (tree-descendants map2)]
       
       (double
        (- 1 (/ similarity-score (+ d1 d2)))))))

(defn similar?
  [doc1 doc2]
  (<= *sim-thresh* (similarity doc1 doc2)))
