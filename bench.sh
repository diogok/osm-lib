#/bin/bash

time java -server -XX:+UseConcMarkSweepGC -XX:+UseCompressedOops -XX:+DoEscapeAnalysis -Xmx4G -cp "$(lein classpath)" clojure.main bench.clj

