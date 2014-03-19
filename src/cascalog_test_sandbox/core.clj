(ns cascalog-test-sandbox.core)

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
