(ns subotai.near-duplicate.shingles
  "Implementation of shingles from the IRBook by Manning et al"
  (:require [subotai.near-duplicate.utils :refer :all])
  (:import [edu.stanford.nlp.process
            CoreLabelTokenFactory
            DocumentPreprocessor
            PTBTokenizer]
           [java.io StringReader]))

(def size-of-grams 4)

(def jaccard-sim-thresh 0.9)

(defn text-n-grams
  [text]
  (let [toks-seq-fn (fn [toks]
                      (map
                       (fn [n]
                         (drop n toks))
                       (range size-of-grams)))]
    (set
     (apply map vector (-> text
                           doc->tokens-seq
                           toks-seq-fn)))))

(defn near-duplicate?
  [text1 text2]
  (let [shingles1 (text-n-grams text1)
        shingles2 (text-n-grams text2)

        common (clojure.set/intersection shingles1
                                         shingles2)

        union  (clojure.set/union shingles1
                                  shingles2)]
    (>= (/ (count common)
           (count union))
        jaccard-sim-thresh)))
