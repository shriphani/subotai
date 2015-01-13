(ns subotai.structural-similarity
  (:require [subotai.structural-similarity.edit-distance :as edit-distance]
            [subotai.structural-similarity.vector-space :as vector-space]))

(def algorithms {:edit-distance edit-distance/similar?
                 :vector-space  vector-space/similar?})

(defn similar?
  "Are the 2 documents structurally similar?
  Default algorithm is the vector-space algorithm"
  ([doc1 doc2]
     (similar? doc1 doc2 :vector-space))

  ([doc1 doc2 algorithm]
     (let [similarity-fn (algorithms algorithm)]
       (similarity-fn doc1 doc2))))
