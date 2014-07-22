(ns subotai.timestamps
  "Detecting timestamps in HTML documents"
  (:use [clj-xpath.core :only [$x:text+ $x:node+]]
        [subotai.dates :as dates]
        [subotai.representation :as representation])
  (:import (org.htmlcleaner HtmlCleaner DomSerializer CleanerProperties)))

(defn timestamps-and-nodes
  "A preliminary listing of timestamps
   on a web-page as picked up by natty."
  [a-html-document]
  (let [nodes-dates (map
                     (fn [n]
                       [n
                        (dates/parse-date
                         (.getTextContent n))])
                     ($x:node+ "//text()"
                               (representation/html->xml-doc a-html-document)))]
    (filter
     (fn [[n ds]]
       (not
        (empty? ds)))
     nodes-dates)))

(defn timestamped-nodes-positions
  [a-html-document]
  (let [timestamped-nodes (timestamps-and-nodes a-html-document)

        nodes-meta-timestamps 
        (map
         (fn [[n ds]]
           (merge (-> n
                      representation/nodes-to-root
                      representation/nodes-and-positions-to-root)
                  {:node  n
                   :dates ds}))
         timestamped-nodes)]

    (group-by :path nodes-meta-timestamps)))

(defn document-detected-timestamps
  "Return a list of dates mined from
   the document"
  [a-html-document]
  (let [timestamped-nodes (timestamps-and-nodes a-html-document)]
    (flatten
     (map
      (fn [[n ds]]
        ds)
      timestamped-nodes))))
