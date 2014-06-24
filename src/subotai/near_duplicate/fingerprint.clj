(ns subotai.near-duplicate.fingerprint
  "A fingerprinting based near-duplicate detection
   algorithm from Charikar et al"
  (:require [digest]
            [subotai.near-duplicate.utils :refer :all]))

(def num-dimensions 64)

(def allowed-bits-differ 3)

(def set-bits-nums (map
                    (fn [i] (bit-set 0 i))
                    (range num-dimensions)))

(defn doc->vector
  [a-document]
  (let [toks (doc->cleaned-non-stopwords-tokens-seq a-document)]
    (reduce
     (fn [acc t]
       (merge-with +' acc {t 1}))
     {}
     toks)))

(defn vector->fingerprint
  "If item at position is positive, then 1
   else 0"
  [vector]
  (reduce
   (fn [x [i v]] 
     (if (pos? v)
       (bit-set x i)
       x))
   (long 0)
   (map-indexed (fn [i v] [i v])
                vector)))

(defn hash-doc-vector
  [a-document]
  (let [doc-vector (doc->vector a-document)

        hash-vector (long-array 64)]
    (doseq [[t w] doc-vector] ; term-freq (weight)
      (let [hashed-value (-> t
                             digest/md5
                             (.getBytes)
                             bigint
                             unchecked-long)]

        (do (doseq [[i x] (map-indexed (fn [i x] [i x]) set-bits-nums)]
              (let [zero-at-pos
                    (zero? (bit-and hashed-value x))]
                (if zero-at-pos
                  (aset hash-vector i (-
                                       (aget hash-vector i)
                                       w)) ; decrement by weight
                  (aset hash-vector i (+
                                       (aget hash-vector i)
                                       w)))))))) ; increment by weight
    hash-vector))

(defn fingerprint
  [a-document]
  (-> a-document
      hash-doc-vector
      vector->fingerprint))

(defn near-duplicate?
  [doc1 doc2]
  (let [f1 (fingerprint doc1)
        f2 (fingerprint doc2)]
    (<= (Long/bitCount
         (bit-xor f1 f2))
        allowed-bits-differ)))
