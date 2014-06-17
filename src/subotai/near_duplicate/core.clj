(ns subotai.near-duplicate.core
  "Near-Duplicate detection algorithms
   for HTML documents"
  (:require [net.cgrand.enlive-html :as html]
            [subotai.near-duplicate.shingles :as shingles])
  (:import [java.io StringReader]))

(def algorithm->fn {:shingles shingles/near-duplicate?})

(defn near-duplicate?
  ([html-doc1 html-doc2]
     (near-duplicate? html-doc1
                      html-doc2
                      :shingles))

  ([html-doc1 html-doc2 algorithm]
   (let [make-resource  #(-> %
                             StringReader.
                             html/html-resource
                             second)

         text1 (html/text
                (make-resource html-doc1))
         text2 (html/text
                (make-resource html-doc2))

         near-dup-fn (algorithm->fn algorithm)]
     (near-dup-fn text1
                  text2))))
