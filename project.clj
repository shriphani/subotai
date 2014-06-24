(defproject subotai "0.2.7"
  :description "Mining HTML documents"
  :url "https://github.com/shriphani/subotai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http "0.7.9"]
                 [com.github.kyleburton/clj-xpath "1.4.2"]
                 [digest "1.4.4"]
                 [edu.stanford.nlp/stanford-parser "3.3.1"]
                 [enlive "1.1.4"]
                 [me.raynes/fs "1.4.4"]
                 [net.sourceforge.htmlcleaner/htmlcleaner "2.6"]
                 [org.clojure/clojure "1.5.1"]])
