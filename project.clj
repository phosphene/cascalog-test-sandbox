(defproject cascalog-test-sandbox "0.1.0-SNAPSHOT"
  :description "testing of cascalog"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"] 
                 [cascalog/cascalog-core "2.0.0"] 
                 [cascalog/cascalog-more-taps "2.0.0"] 
                 [cascalog/cascalog-checkpoint "2.0.0"]
                 [org.apache.lucene/lucene-core "4.7.0"]
                 [org.apache.lucene/lucene-analyzers-common "4.7.0"]
                 [org.apache.mahout/mahout-core "0.9"]
                 [org.apache.mahout/mahout-math "0.9"]
                 ]
  :profiles {:dev 
             {:dependencies 
              [[speclj "2.5.0"] 
               [midje "1.6.0"]
               [org.apache.hadoop/hadoop-core "1.1.2"]
               [cascalog/midje-cascalog "2.0.0"]
               [lein-midje "3.1.3"]] 
              }
             }
  :plugins [[lein-midje "3.1.3"] [speclj "2.5.0"]]
  :test-paths ["test/"]
  :repositories {"conjars" "http://conjars.org/repo"}
  :jvm-opts ["-Xms512m" "-Xmx768m"]
  :main cascalog-test-sandbox.core
  )
