(ns subotai.structural-similarity.xpath-text
  "Compute XPaths to text and use cosine similarity
   as a metric"
  (:require [subotai.structural-similarity.utils :as utils]
            [clojure.string :as string])
  (:use [clj-xpath.core :only [$x:node+]])
  (:import [com.google.common.base CharMatcher]))

(def *sim-thresh* 0.58)

(defn node->xpath-component
  "Supplied a node, we produce an xpath component bruh. Make it
   a text-node pls"
  [a-node]
  (let [name  (.getNodeName a-node)
        class (utils/fix-class-attribute
               (utils/node-attr a-node
                                "class"))]
    (if-not class
      name
      (format "%s[contains(@class, '%s')]" name class))))

(defn page-text-xpaths
  "Accepts a html document and produces a
   list of XPaths with the associated text"
  [a-doc]
  (and
   a-doc
   (let [xml-document (utils/html->xml a-doc)
         text-nodes   (utils/text-nodes xml-document)]
     (map
      (fn [t]
        (let [nodes-to-root    (drop-last ; last node is named #text
                                (utils/nodes-to-root t))
              xpath-components (concat
                                (map node->xpath-component
                                     nodes-to-root)
                                ["text()"])
              node-text        (.trimFrom CharMatcher/WHITESPACE
                                          (.getNodeValue t))]
          [(string/join "/" (cons "/" xpath-components)) node-text]))
      text-nodes))))

(defn char-frequency-representation
  "Provide a set of xpath and text pairs,
   this representation returns (xpath, char) pairs"
  [text-xpaths-coll]
  (reduce
   (fn [acc [x text]]
     (merge-with +' acc {x (count text)}))
   {}
   text-xpaths-coll))

(defn similarity-cosine-char-freq
  [doc1 doc2]
  (let [r1 (char-frequency-representation
            (page-text-xpaths doc1))
        r2 (char-frequency-representation
            (page-text-xpaths doc2))]
    (utils/cosine-similarity r1 r2)))

(defn similar?
  [doc1 doc2]
  (try (<= *sim-thresh*
           (similarity-cosine-char-freq doc1
                                        doc2))
       (catch org.w3c.dom.DOMException e false)))
