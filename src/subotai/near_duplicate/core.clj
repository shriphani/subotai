(ns subotai.near-duplicate.core
  "Near-Duplicate detection algorithms
   for HTML documents"
  (:require [subotai.near-duplicate.fingerprint :as fingerprint]
            [subotai.near-duplicate.shingles :as shingles])
  (:import [java.io StringReader]
           [org.htmlcleaner HtmlCleaner DomSerializer CleanerProperties]))

(def algorithm->fn {:shingles shingles/near-duplicate?
                    :fingerprint fingerprint/near-duplicate?})

(defn near-duplicate-text?
  ([doc1 doc2]
     (near-duplicate-text? doc1
                           doc2
                           :fingerprint))

  ([doc1 doc2 algorithm]
     (let [near-dup-fn (algorithm->fn algorithm)]
       (near-dup-fn doc1
                    doc2))))

(defn html->text
  [a-html-doc]
  (let [cleaner (HtmlCleaner.)
        props   (.getProperties cleaner)
        _ (.setPruneTags props "script, style")
        _ (.setOmitComments props true)]
    (-> cleaner
        (.clean a-html-doc)
        .getText
        .toString)))

(defn near-duplicate-html?
  ([html-doc1 html-doc2]
     (near-duplicate-html? html-doc1
                           html-doc2
                           :fingerprint))

  ([html-doc1 html-doc2 algorithm]
   (let [text1 (html->text html-doc1)
         text2 (html->text html-doc2)]
     (near-duplicate-text? text1
                           text2
                           algorithm))))
