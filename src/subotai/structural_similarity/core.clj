(ns subotai.structural-similarity.core
  (:require [subotai.structural-similarity.edit-distance :as edit-distance]
            [subotai.structural-similarity.xpath-text :as xpath-text]))

(def similar-algorithms {:edit-distance edit-distance/similar?
                         :xpath-text    xpath-text/similar?})

(defn similar?
  "Are the 2 documents structurally similar.
   By default, the algorithm used in RTDM edit
   distance"
  ([doc1 doc2]
     (similar? doc1 doc2 :edit-distance))

  ([doc1 doc2 algorithm]
     (let [similarity-fn (similar-algorithms algorithm)]
       (similarity-fn doc1 doc2))))
