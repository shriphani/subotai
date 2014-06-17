(ns subotai.near-duplicate.shingles
  "Implementation of shingles from the IRBook by Manning et al"
  (:import [edu.stanford.nlp.process
            CoreLabelTokenFactory
            DocumentPreprocessor
            PTBTokenizer]
           [java.io StringReader]))

(def size-of-grams 4)

(def jaccard-sim-thresh 0.9)

(defn doc->tokens-seq
  "Converts a doc to a sequence
   of token strings"
  [text]
  (let [ptb-tok (PTBTokenizer. (StringReader. text)
                               (CoreLabelTokenFactory.)
                               "")]
    (take-while
     identity
     (repeatedly
      (fn []
        (if (.hasNext ptb-tok)
          (-> ptb-tok .next .toString)
          nil))))))

(defn near-duplicate?
  [text1 text2]
  (let [toks1 (doc->tokens-seq text1)
        toks2 (doc->tokens-seq text2)

        shingles1 (set
                   (partition size-of-grams toks1))
        shingles2 (set
                   (partition size-of-grams toks2))

        common (clojure.set/intersection shingles1
                                         shingles2)

        union  (clojure.set/union shingles1
                                  shingles2)]
    (>= (/ (count common)
           (count union))
        jaccard-sim-thresh)))
