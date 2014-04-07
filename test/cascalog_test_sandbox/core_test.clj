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


(fact (word-count-split short-sentences) => (produces short-wordcounts))

;;(fact (word-count short-sentences) => (produces short-wordcounts))

;;        (provided
;;         (hfs-textline :path) => [["another another word"]]))) ;; true




;; the fact?<- idiom has been deprecated
;; therefore this test no long passes
;; when =wc-query= is called with =:text-path=
;; it will produce =short-wordcounts=,
;; provided =(hfs-textline :text-path)= produces =short-sentences=.
;;(fact "hmmm" (wc-query :text-path) => (produces short-wordcounts)
;;      (provided
;;       (hfs-textline :text-path) => (produces short-sentences))) ;; true


;;(fact "huh?" (wc-query :text-path) => (produces short-sentences))


;;(let [sentence [["two words"]]
;;      words    [["two"] ["words"]]]
;;  (fact  (split sentence) => words))


(let [counts [["word" 1] ["another" 2]]]
  (fact (wc-query :path) => (produces counts)
        (provided
         (hfs-textline :path) => [["another another word"]]))) ;; true







;;; if the split function should break outside of a cascalog query
;;; then why does this work?
;;(fact "split should produce a charsequence of substrings"
 ;;     (let [result (split "this is a sentence")]
 ;;      result => ["this" "is" "a" "sentence"]))




(fact (scrub-text "FoO BAR ") => "foo bar")


(let [rain [["doc1" "a b c"]]
      stop [["b"]]]
  (fact
   (etl-docs-gen rain stop) => (produces [["doc1" "a"]
                                          ["doc1" "c"]])))



;;(def short-sentences
;;  [["this is a sentence sentence"]
;;   ["sentence with this is repeated"]])


;;(fact (tokenize-strings short-sentences "outpath") => (produces [["test"]])) 




      





