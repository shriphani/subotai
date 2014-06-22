(ns subotai.near-duplicate.utils
  "Common utils for the near-duplicate codebase"
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [edu.stanford.nlp.process
            CoreLabelTokenFactory
            DocumentPreprocessor
            Morphology
            PTBTokenizer]
           [java.io StringReader]))

(def stoplist-file "stoplist.dft")

(def stoplist (set
               (string/split-lines
                (slurp
                 (io/resource stoplist-file)))))

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

(defn doc->cleaned-tokens-seq
  "Converts a doc to a sequence of
   case-folded, punctuation-removed,
   stemmed tokens"
  [text]
  (let [toks-seq (doc->tokens-seq text)

        morph (Morphology.)]
    (map
     (fn [t]
       (->> t
            string/lower-case
            (.stem morph)))
     (filter
      (fn [t]
        (not
         (re-find #"\p{Punct}" t)))
      toks-seq))))

(defn doc->cleaned-non-stopwords-tokens-seq
  [text]
  (let [cleaned-tokens-seq (doc->cleaned-tokens-seq text)]
    (filter
     (fn [t]
       (not
        (some #{t} stoplist)))
     cleaned-tokens-seq)))
