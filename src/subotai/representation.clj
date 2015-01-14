(ns subotai.representation
  "Module that implements several HTML
   document representations used in a variety of
   data-mining algorithms"
  (:require [clojure.string :as string])
  (:use [clj-xpath.core :only [$x:text+ $x:node+]]
        [subotai.dates :as dates]
        [subotai.structural-similarity.utils :as utils]))

(defn trim-path-to-html
  "Libraries often have dummy document tags
  as doc-roots. We would rather our selectors
  and links begin with the html tag"
  [a-path]
  (drop-while #(-> %
                   first
                   (= "html")
                   not)
              a-path))

(defn group-links
  "Groups similar links together.
  Assigns a selector (or XPath) to links
  and groups the links by these xpaths"
  [page-src]
  (let [processed-page (utils/process-page page-src)
        grouped-nodes  (partition
                        2
                        (utils/tree-walk-vector-space processed-page
                                                      []
                                                      (fn [a-node]
                                                        (when (=
                                                               (.nodeName a-node)
                                                               "a")
                                                          (.attr a-node
                                                                 "href")))))
        grouped-links
        (filter second grouped-nodes)

        fixed-path-grouped-links (map
                                  (fn [[path link]]
                                    [(trim-path-to-html path) link])
                                  grouped-links)]
    (reduce
     (fn [grouped [path link]]
       (merge-with concat grouped {path [link]}))
     {}
     fixed-path-grouped-links)))

(defn group-links-with-selectors
  "Generate grouped links and use selectors"
  [page-src]
  (let [paths-links (group-links page-src)]
    (into
     {}
     (map
      (fn [[path links]]
        [(utils/path->css-selector path) links])
      paths-links))))
