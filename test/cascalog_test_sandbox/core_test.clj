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
