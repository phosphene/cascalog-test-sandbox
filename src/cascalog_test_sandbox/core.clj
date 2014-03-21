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
