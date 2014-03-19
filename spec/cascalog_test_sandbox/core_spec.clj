(ns cascalog-test-sandbox.core-spec
  (:require [speclj.core :refer :all]
            [cascalog-test-sandbox.core :refer :all]))

(describe "Truth"

  (it "is true"
    (should true))

  (it "is not false"
    (should-not false)))

(run-specs)
