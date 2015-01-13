(ns subotai.structural-similarity.utils
  (:require [clj-http.client :as client]
            [clojure.set :as clj-set]
            [clojure.string :as string])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]
           [org.jsoup.nodes Element]
           [org.jsoup.select Elements]))

(defn page-body
  [a-link]
  (try (-> a-link client/get :body)
       (catch Exception e nil)))

(defn process-page
  "Parse a webpage using the HTML lib and
   return the parsed object"
  [page-src]
  (Jsoup/parse page-src))

(defn anchor-nodes
  "Returns a seq of anchor
   tags enclosed in a HTML node"
  [a-node]
  (.getElementsByTag a-node "a"))

(defn nodes-to-root
  "Returns nodes from document root to
   supplied argument (included)"
  [a-node]
  (reverse
   (take-while identity
               (iterate
                (fn [x]
                  (.parent x))
                a-node))))

(defn node-attr
  [a-node key]
  (.attr a-node key))

(defn remove-trailing-digits
  [a-string]
  (let [trailing-digits (re-find #"\d*$" a-string)]
    (string/replace a-string trailing-digits "")))

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

(defn fix-string
  [a-string]
  (-> a-string string/trim))

(defn tree-walk-vector-space
  "Walks a tree and generates a vector space
  representation of the page.
  Example vector space representation:
  [s chars-at-s]"
  ([root]
   (partition 2 (tree-walk-vector-space root [] #(.text %))))

  ([root path-so-far leaf-op]
   (let [children  (.children root)
         path-item [(.nodeName root) (.className root)]
         updated-path (conj path-so-far path-item)]

     (if (empty? children)
       [updated-path (leaf-op root)]
       (mapcat
        (fn [c]
          (tree-walk-vector-space c
                                  updated-path
                                  leaf-op))
        children)))))
