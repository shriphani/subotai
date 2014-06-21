(ns subotai.near-duplicate.fingerprint
  "A fingerprinting based near-duplicate detection
   algorithm from Charikar et al"
  (:require [clojure.string :as string]
            [subotai.near-duplicate.utils :refer :all])
  (:import [edu.stanford.nlp.process
            CoreLabelTokenFactory
            DocumentPreprocessor
            Morphology
            PTBTokenizer]
           [java.io StringReader]))

(defn doc->features
  [a-document]
  (let [morph  (Morphology.)
        tokens (map
                (fn [t]
                  (.stem morph t))
                (map
                 string/lower-case
                 (filter
                  (fn [t]
                    (not
                     (re-find #"\p{Punct}" t)))
                  (doc->tokens-seq a-document))))]
    (reduce
     (fn [acc t]
       (merge-with + acc {t 1}))
     {}
     tokens)))

;; (defn doc->fingerprint
;;   [a-document]
;;   (let [features (doc->features a-document)]
;;     ))
