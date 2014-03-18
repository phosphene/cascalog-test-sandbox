(defproject cascalog_spec_sandbox "0.1.0-SNAPSHOT"
  :description "spec testing of cascalog"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"] [cascalog/cascalog-core "2.0.0"]]
  :profiles {:dev 
             {:dependencies 
              [[speclj "2.5.0"] 
               [org.apache.hadoop/hadoop-core "1.1.2"]
               [lein-midje "3.0.1"]
               [cascalog/midje-cascalog "1.10.1"]] 
              }
             }
  :plugins [[speclj "2.5.0"]]
  :test-paths ["spec"]
  :repositories {"conjars" "http://conjars.org/repo"}
  :jvm-opts ["-Xms768m" "-Xmx768m"]
  )
