(ns cascalog-test-sandbox.core
(:use cascalog.api)
(:require [cascalog.logic.ops :as c]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


;;; This is an incorrect implementation, such as might be written by
;;; someone who was used to a Lisp in which an empty list is equal to
;;; nil.


(defn first-element [sequence default]
  (if (nil? sequence)
    default
    (first sequence)))


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

(defmapcatop split
  "Accepts a sentence 1-tuple, splits that sentence on whitespace, and
  emits a single 1-tuple for each word."
  [^String sentence]
  (seq (.split sentence "\\s+")))

(defn wc-query
  "Returns a subquery that generates counts for every word in
    the text-files located at `text-path`."
  [text-path]
  (let [src (hfs-textline text-path)]
    (<- [?word ?count]
        (src ?textline)
        (split ?textline :> ?word)
        (c/count ?count))))

(defn -main
  "Accepts the following arguments:

   - text-path (path to a textfile, or directory with textfiles)
   - results-path (location of textfile containing results)

     And prints lines of the form \"word count\" to a textfile at
     results-path. Each distinct word in the textfiles at text-path
     gets a count."
  [text-path results-path]
  (?- (hfs-textline results-path)
      (wc-query text-path)))


