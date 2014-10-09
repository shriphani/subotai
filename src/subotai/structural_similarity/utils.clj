(ns subotai.structural-similarity.utils
  (:require [clj-http.client :as client]
            [clojure.set :as clj-set]
            [clojure.string :as string])
  (:import [org.htmlcleaner HtmlCleaner DomSerializer CleanerProperties]))

(defn page-body
  [a-link]
  (try (-> a-link client/get :body)
       (catch Exception e nil)))

(defn html->xml
  "Converts a html string to an XML object"
  [page-src]
  (let [cleaner (new HtmlCleaner)
        props   (doto (.getProperties cleaner)
                  (.setPruneTags "script, style")
                  (.setOmitComments true))
        cleaned (.clean cleaner page-src)]
    cleaned))

(defn anchor-nodes
  [a-node]
  (.getElementsByName a-node
                      "a"
                      true))

(defn nodes-to-root
  "Returns nodes from document root to
   supplied argument (included)"
  [a-node]
  (reverse
   (take-while identity
               (iterate
                (fn [x]
                  (.getParent x))
                a-node))))

(defn node-attr
  [a-node key]
  (.getAttributeByName a-node key))

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

(defn fix-string
  [a-string]
  (-> a-string string/trim))

(defn tree-walk-vector-space
  "Walks a tree and generates a vector space
   representation of the desired stuff"
  ([root path-so-far]
     (tree-walk-vector-space root
                             path-so-far
                             #(-> % (.getText) (.toString) fix-string)))
  ([root path-so-far leaf-op]
   (let [children  (.getChildTags root)
         path-item [(.getName root)
                    (fix-class-attribute
                     (node-attr root "class"))]]
    
     (cond (empty? children)
           [(conj path-so-far path-item)
            (leaf-op root)]

           :else
           (mapcat
            (fn [c]
              (tree-walk-vector-space c
                                      (conj path-so-far
                                            path-item)))
            children)))))
