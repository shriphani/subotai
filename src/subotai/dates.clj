(ns subotai.dates
  "Wrapper around natty that speaks
   joda-time (and thus can be used with
   clj-time code)"
  (:require [clj-time.coerce :as c])
  (:import [com.joestelmach.natty Parser]))

(defn parse-date
  [text]
  (let [parser (Parser.)
        groups (.parse parser text)

        dates (flatten
               (map
                (fn [g]
                  (map
                   (fn [d]
                     (c/from-long d))
                   (.getDates g)))
                groups))]
    dates))
