(ns subotai.structural-similarity.vector-space
  "Convert a webpage into vectors and use cosine
  similarities"
  (:require [subotai.structural-similarity.utils :as utils]
            [clojure.string :as string])
  (:import [com.google.common.base CharMatcher]))

(def *sim-thresh* 0.58)

(defn path->xpath
  [path]
  (let [path-segment (fn [[tag class]]
                       (if (or (string/blank? class)
                               (nil? class))
                         tag
                         (str tag "[contains(@class, '" class "')]")))]
   (string/join "/" (map path-segment path))))

(defn page-vectors
  "Accepts a html document and produces a
   list of XPaths with the associated text"
  [a-doc]
  (let [paths-text (-> a-doc
                       utils/process-page
                       utils/tree-walk-vector-space)]
    (reduce
     (fn [acc [path text]]
       (merge-with +' acc {(path->xpath path)
                           (if (nil? text)
                             0
                             (count
                              (.trimFrom CharMatcher/WHITESPACE text)))}))
     {}
     paths-text)))

(defn similarity-cosine-char-freq
  [doc1 doc2]
  (let [r1 (page-vectors doc1)
        r2 (page-vectors doc2)]
    (utils/cosine-similarity r1 r2)))

(defn similar?
  [doc1 doc2]
  (try (<= *sim-thresh*
           (similarity-cosine-char-freq doc1
                                        doc2))
       (catch org.w3c.dom.DOMException e false)))
