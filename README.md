# cascalog-test-sandbox

A Clojure library designed to test drive the development of cascalog applications.

This sandbox uses midje-cascalog and its idioms to test drive the development of Hadoop/cascalog/cascading jobs.

Examples include use of Lucene classes

Mahout is included in the project. implementation and testing of Mahout coming soon.... 


## Usage

leiningen must be installed

$ lein repl

cascalog-test-sandbox.core=> (use 'midje.repl)

cascalog-test-sandbox.core=> (autotest)

tests will run on any change in files

start playing in the sandbox

## License

attributions are made where notable.

Copyright © 2014 Phosphene

Distributed under the Eclipse Public License either version 1.0.
