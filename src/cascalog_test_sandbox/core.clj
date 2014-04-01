(ns cascalog-test-sandbox.core
  (:use [cascalog.api]
        [cascalog.more-taps :only (hfs-delimited)]
        [cascalog.checkpoint])
  (:require [cascalog.logic.ops :as c]
            [cascalog.logic.def :as def]
            [cascalog.logic.vars :as v]
            [clojure.string :as s])
  (:import
   org.apache.lucene.analysis.standard.StandardAnalyzer
   org.apache.lucene.analysis.TokenStream
   org.apache.lucene.util.Version
   org.apache.lucene.analysis.tokenattributes.TermAttribute)
  )


;;placeholder function
(defn mysubquery [datastore-path])

;;; this sample function is from midje-cascalog testing docs 
;;; https://github.com/nathanmarz/cascalog/tree/develop/midje-cascalog

(defn max-followers-query [datastore-path]
  (let [src (name-vars (mysubquery datastore-path)
                       ["?user" "?follower-count"])]
    (c/first-n src 1 :sort ["?follower-count"] :reverse true)))

;; from http://sritchie.github.io/2012/01/22/cascalog-testing-20/

(def src
  [[1 2] [1 3]
   [3 4] [3 6]
   [5 2] [5 9]])

;; adds the values in each input tuple, sorts the output and returns
;; 2-tuples of the first number and the sum. [1 2] becomes [1 3], for
;; example.
(def query
  (<- [?x ?sum]
      (src ?x ?y)
      (:sort ?x)
      (c/sum ?y :> ?sum)))

;;;word count example below frm http://sritchie.github.io/2011/09/30/testing-cascalog-with-midje/

(def/defmapcatfn tokenise [line]
  "reads in a line of string and splits it by a regular expression"
  (s/split line #"[\[\]\\\(\),.)\s]+"))

;;same function same regex
;;this is a repeat
(def/defmapcatfn split [line]
  "reads in a line of string and splits it by regex"
  (s/split line #"[\[\]\\\(\),.)\s]+"))


;; (def/defmapcatfn split
;;   "Accepts a sentence 1-tuple, splits that sentence on whitespace, and
;;   emits a single 1-tuple for each word."
;;   [^String sentence]
;;   (seq (clojure.string/split sentence #"\s+")))

;; example query

(defn wc-query
  "Returns a subquery that generates counts for every word in
    the text-files located at `text-path`."
  [text-path]
  (let [src (hfs-textline text-path)]
    (<- [?word ?count]
        (src ?textline)
        (split ?textline :> ?word)
        (c/count ?count))))

;;(defn -main
  
 ;;; [text-path results-path]
 ;; (?- (hfs-textline results-path)
 ;;     (wc-query text-path)))


(defn scrub-text [s]
  "trim open whitespaces and lower case"
  ((comp s/trim s/lower-case) s))

(defn assert-tuple [pred msg x]
  "helper function to add assertion to tuple stream"
  (when (nil? (assert (pred x) msg))
    true))

(def assert-doc-id ^{:doc "assert doc-id is correct format"}
  (partial assert-tuple #(re-seq #"doc\d+" %) "unexpected doc-id"))

(defn etl-docs-gen [rain stop]
  (<- [?doc-id ?word]
      (rain ?doc-id ?line)
      (split ?line :> ?word-dirty)
      (scrub-text ?word-dirty :> ?word)
      (stop ?word :> false)
      (assert-doc-id ?doc-id)
      (:trap (hfs-textline "output/trap" :sinkmode :update))))

(defn word-count [src]
  "simple word count across all documents"
  (<- [?word ?count]
      (src _ ?word)
      (c/count ?count)))

;;split and word count
(defn word-count-split [src]
  "word count and split each line in tuples"
  (<- [?word ?count]
      (src ?line)
      (tokenise ?line :> ?word)
      (c/count ?count)))


(defn D [src]
  (let [src (select-fields src ["?doc-id"])]
    (<- [?n-docs]
        (src ?doc-id)
        (c/distinct-count ?doc-id :> ?n-docs))))

(defn DF [src]
  (<- [?df-word ?df-count]
      (src ?doc-id ?df-word)
      (c/distinct-count ?doc-id ?df-word :> ?df-count)))

(defn TF [src]
  (<- [?doc-id ?tf-word ?tf-count]
      (src ?doc-id ?tf-word)
      (c/count ?tf-count)))

(defn tf-idf-formula [tf-count df-count n-docs]
  (->> (+ 1.0 df-count)
    (div n-docs)
    (Math/log)
    (* tf-count)))

(defn TF-IDF [src]
  (let [n-doc (first (flatten (??- (D src))))]
    (<- [?doc-id ?tf-idf ?tf-word]
        ((TF src) ?doc-id ?tf-word ?tf-count)
        ((DF src) ?tf-word ?df-count)
        (tf-idf-formula ?tf-count ?df-count n-doc :> ?tf-idf))))

(defn -main [in out stop tfidf & args]
  (workflow
    ["tmp/checkpoint"]
    etl-step ([:tmp-dirs etl-stage]
              (let [rain (hfs-delimited in :skip-header? true)
                    stop (hfs-delimited stop :skip-header? true)]
                (?- (hfs-delimited etl-stage)
                    (etl-docs-gen rain stop))))
    tf-step ([:deps etl-step]
              (let [src (name-vars (hfs-delimited etl-stage :skip-header? true) ["?doc-id" "?word"])]
                (?- (hfs-delimited tfidf)
                    (TF-IDF src))))
    wrd-step ([:deps etl-step]
              (?- (hfs-delimited out)
                  (word-count (hfs-delimited etl-stage))))))
