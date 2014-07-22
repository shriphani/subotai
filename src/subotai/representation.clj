(ns subotai.representation
  "Module that implements several HTML
   document representations used in a variety of
   data-mining algorithms"
  (:require [clojure.string :as string])
  (:use [clj-xpath.core :only [$x:text+ $x:node+]]
        [subotai.dates :as dates])
  (:import (org.htmlcleaner HtmlCleaner DomSerializer CleanerProperties)))

(defn process-page
  [page-src]
  (let [cleaner (new HtmlCleaner)
        props   (.getProperties cleaner)
        _       (.setPruneTags props "script, style")
        _       (.setOmitComments props true)]
    (.clean cleaner page-src)))

(defn html->xml-doc
  "Take the html and produce an xml version"
  [page-src]
  (let [tag-node       (-> page-src
                           process-page)

        cleaner-props  (new CleanerProperties)

        dom-serializer (new DomSerializer cleaner-props)]
    
    (-> dom-serializer
        (.createDOM tag-node))))

(defn nodes-to-root
  "Build a path from current node
   to the document root"
  ([a-node]
     (nodes-to-root a-node
                    []))
  
  ([a-node current-path]
     (let [parent (.getParentNode a-node)]
      (if (not= (.getNodeName parent)
                "#document")
        (recur parent (cons a-node current-path))
        (cons a-node current-path)))))

(defn node-class
  "Value of the class attribute of a node.
   We split on hyphens and underscores and
   remove trailing digits to account for
   weirdness"
  [a-node]
  (let [existing (try (-> a-node
                          (.getAttributes)
                          (.getNamedItem "class")
                          (.getValue))
                      (catch Exception e nil))]
    (if-not (nil? existing)
      (string/replace
       (first
        (string/split existing #"-|_|\s+"))
       #"\d+$"
       "")
      nil)))

(defn child-position
  [parent a-w3c-node]
  (let [child-node-list (.getChildNodes parent)
        child-nodes-cnt (.getLength child-node-list)
        child-nodes     (filter
                         (fn [a-node]
                           (and (= (.getNodeName a-node)
                                   (.getNodeName a-w3c-node))
                                (= (node-class a-node)
                                   (node-class a-w3c-node))))
                         (map
                          #(.item child-node-list %)
                          (range child-nodes-cnt)))]
    (.indexOf
     (map
      #(.isSameNode % a-w3c-node)
      child-nodes)
     true)))

(defn nodes-and-positions-to-root
  "Build a path from nodes to root
   positions
   Args: a list of nodes with parent-child relationships"
  [a-node-path]
  (let [nodes-parents (map vector a-node-path (rest a-node-path))]
    {:path      (map
                 (fn [n]
                   [(.getNodeName n)
                    (node-class n)])
                 a-node-path)
     :positions (map
                 (fn [[p n]]
                   (child-position p n))
                 nodes-parents)}))
