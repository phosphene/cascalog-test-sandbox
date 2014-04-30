(ns cascalog-test-sandbox.core-test
  (:use [cascalog-test-sandbox.core]
        [cascalog.api]
        [midje sweet cascalog])
)

;;sample test

(fact "Query should return a single tuple containing
           [most-popular-user, follower-count]."
      (max-followers-query :path) => (produces [["richhickey" 2961]])
      (provided
      (mysubquery :path) => [["sritchie09" 180]
                              ["richhickey" 2961]]))

(facts "query produces 2-tuples from src defined in core.clj"
  query => (produces [[3 10] [1 5] [5 11]])  ;; true
  query => (produces [[1 5] [3 10] [5 11]])) ;; true

(fact
  query =not=> (produces [["string!" 11] [1 5] [5 11]])) ;; true

(fact
  query =not=> (produces [["string!" 11] [1 5] [5 11]])) ;; true


(facts
  query => (produces-suffix [[5 11]]) ;; true
  query => (produces-prefix [[1 5]])) ;; true

;; this is a test of the canonical cascading word-count example
;; we create our test data using the let syntax, which integrates nicely
;; with midje


(let [short-sentences
      [["this is a sentence sentence"]
       ["sentence with this is repeated"]]
      short-wordcounts
      [["sentence" 3]
       ["repeated" 1]
       ["is" 2]
       ["a" 1]
       ["this" 2]
       ["with" 1]]]
  (fact (word-count-split short-sentences) => (produces short-wordcounts)))


;;; this note concerns the deprecation of syntax
;;; we have kept this here as documentation and notice
;; the fact?<- idiom has been deprecated
;; therefore this test no long passes
;; when =wc-query= is called with =:text-path=
;; it will produce =short-wordcounts=,
;; provided =(hfs-textline :text-path)= produces =short-sentences=.
;;(fact "hmmm" (wc-query :text-path) => (produces short-wordcounts)
;;      (provided
;;       (hfs-textline :text-path) => (produces short-sentences))) ;; true


;; this test uses the provided affordance to abstract away what is *not* tested

(let [counts [["word" 1] ["another" 2]]]
  (fact (wc-query :path) => (produces counts)
        (provided
         (hfs-textline :path) => [["another another word"]]))) ;; true



;; these tests are for etl-docs-gen

(fact (scrub-text "FoO BAR ") => "foo bar")


(let [rain [["doc1" "a b c"]]
      stop [["b"]]]
  (fact
   (etl-docs-gen rain stop) => (produces [["doc1" "a"]
                                          ["doc1" "c"]])))






