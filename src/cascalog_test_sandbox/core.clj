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
   org.apache.lucene.analysis.tokenattributes.CharTermAttribute
   [java.io File]
   [org.apache.mahout.cf.taste.impl.model.file FileDataModel]
   [org.apache.mahout.cf.taste.impl.similarity PearsonCorrelationSimilarity]
   [org.apache.mahout.cf.taste.impl.recommender GenericUserBasedRecommender]
   [org.apache.mahout.cf.taste.impl.neighborhood NearestNUserNeighborhood]

   [org.apache.mahout.cf.taste.eval RecommenderBuilder RecommenderIRStatsEvaluator]
   [org.apache.mahout.cf.taste.impl.eval
    AverageAbsoluteDifferenceRecommenderEvaluator RMSRecommenderEvaluator
    GenericRecommenderIRStatsEvaluator]
   [org.apache.mahout.common RandomUtils])
  )

;; we have imported more than we are currently using
;; the thought is to eventually test some use of all these libraries

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

;;latest cascalog api
(def/defmapcatfn tokenise [line]
  "reads in a line of string and splits it by a regular expression"
  (s/split line #"[\[\]\\\(\),.)\s]+"))

;;same function same regex
;;this is a repeat
(def/defmapcatfn split [line]
  "reads in a line of string and splits it by regex"
  (s/split line #"[\[\]\\\(\),.)\s]+"))


(defn wc-query
  "Returns a subquery that generates counts for every word in
    the text-files located at `text-path`."
  [text-path]
  (let [src (hfs-textline text-path)]
    (<- [?word ?count]
        (src ?textline)
        (split ?textline :> ?word)
        (c/count ?count))))

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


;; the following is modified from 
;; https://github.com/sorenmacbeth/lucene-cascalog-demo

(defn tokenizer-seq
  "Build a lazy-seq out of a tokenizer with TermAttribute"
  [^TokenStream tokenizer ^CharTermAttribute term-att]
  (lazy-seq
    (when (.incrementToken tokenizer)
      (cons (.term term-att) (tokenizer-seq tokenizer term-att)))))

(defn load-analyzer [^java.util.Set stopwords]
  (StandardAnalyzer. Version/LUCENE_CURRENT stopwords))

(defn tokenize-text
  "Apply a lucene tokenizer to cleaned text content as a lazy-seq"
  [^StandardAnalyzer analyzer page-text]
  (let [reader (java.io.StringReader. page-text)
        tokenizer (.tokenStream analyzer nil reader)
        term-att (.addAttribute tokenizer CharTermAttribute)]
    (tokenizer-seq tokenizer term-att)))

(defn emit-tokens [tokens-seq]
  "Compute n-grams of a seq of tokens"
  (partition 1 1 tokens-seq))

(def/defmapcatfn tokenize-string {:stateful true}
  ([] (load-analyzer StandardAnalyzer/STOP_WORDS_SET))
  ([analyzer text]
     (emit-tokens (tokenize-text analyzer text)))
   ([analyzer] nil))

(defn tokenize-strings [in-path out-path]
  (let [src (hfs-textline in-path)]
    (?<- (hfs-textline out-path :sinkmode :replace)
         [!line ?token]
         (src !line)
         (tokenize-string !line :> ?token)
         (:distinct false))))

