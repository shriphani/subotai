(ns subotai.near-duplicate.utils
  "Common utils for the near-duplicate codebase"
  (:import [edu.stanford.nlp.process
            CoreLabelTokenFactory
            DocumentPreprocessor
            PTBTokenizer]
           [java.io StringReader]))

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
