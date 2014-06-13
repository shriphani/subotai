(ns subotai.structural-similarity.utils
  (:require [clj-http.client :as client]
            [clojure.set :as clj-set]
            [clojure.string :as string])
  (:use [clj-xpath.core :only [$x:node+]])
  (:import [org.htmlcleaner HtmlCleaner DomSerializer CleanerProperties]))

(defn page-body
  [a-link]
  (try (-> a-link client/get :body)
       (catch Exception e nil)))

(defn html->xml
  "A html document is converted to an XML object"
  [a-html-doc]
  (let [cleaner (new HtmlCleaner)
        
        cleaner-props (new CleanerProperties)
        dom-srlzr     (new DomSerializer cleaner-props)
        
        cleaned-doc   (.clean cleaner a-html-doc)]
    
    (.createDOM dom-srlzr cleaned-doc)))

(defn anchor-nodes
  [a-node]
  (try ($x:node+ ".//a" a-node)
       (catch RuntimeException e [])))

(defn text-nodes
  [a-node]
  (try ($x:node+ ".//text()" a-node)
       (catch RuntimeException e [])))

(defn nodes-to-root
  "Returns nodes from document root to
   supplied argument (included)"
  [a-node]
  (drop
   1 ; we are dropping the first ele since it is not html
   (reverse
    (take-while identity
                (iterate
                 (fn [x]
                   (.getParentNode x))
                 a-node)))))

(defn node-attr
  [a-node key]
  (try
   (-> a-node
       (.getAttributes)
       (.getNamedItem key)
       (.getValue))
   (catch NullPointerException e nil)))

(defn remove-trailing-digits
  [a-string]
  (let [trailing-digits (re-find #"\d*$" a-string)]
    (string/replace a-string trailing-digits "")))

(defn fix-class-attribute
  "We produce a class attribute that matters"
  [a-class-attr]
  (and
   a-class-attr
   (first
    (let [potential-classes (string/split a-class-attr #"-|_|\s+")]
      (map remove-trailing-digits potential-classes)))))

(defn magnitude
  [x]
  (Math/sqrt
   (apply + (map
             (fn [[k v]]
               (* v v))
             x))))

(defn inner-product
  [x y]
  (let [ks (clj-set/union (set (keys x))
                          (set (keys y)))]
    (apply + (map
              (fn [k]
                (if (and (x k) (y k))
                  (* (x k) (y k)) 0))
              ks))))

(defn cosine-similarity
  [x y]
  (let [inner-prod (inner-product x y)
        mod-x      (magnitude x)
        mod-y      (magnitude y)]
    (if (or (zero? mod-x)
            (zero? mod-y))
      0
      (/ inner-prod (* mod-x mod-y)))))
