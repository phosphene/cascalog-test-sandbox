(ns cascalog-test-sandbox.core-test
  
  (:use [cascalog-test-sandbox.core]
        [cascalog.api]
        [midje sweet cascalog])
)

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
  query => (produces-suffix [[5 11]])        ;; true
  query => (produces-prefix [[1 5]])) ;; true


(def short-sentences
  [["this is a sentence sentence"]
   ["sentence with this is repeated"]])

(def short-wordcounts
  [["sentence" 3]
   ["repeated" 1]
   ["is" 2]
   ["a" 1]
   ["this" 2]
   ["with" 1]])

;; when =wc-query= is called with =:text-path=
;; it will produce =short-sentences=,
;; provided =(hfs-textline :text-path)= produces =short-wordcounts=.
(fact "hmmm"  
short-wordcounts (wc-query :text-path)
        (provided
          (hfs-textline :text-path) => short-sentences)) ;; true
