#/bin/bash

[[ ! -e "data/brazil.osm.bz2" ]] && mkdir data && echo "Download brazil.osm.bz2"

time java -server -XX:+UseConcMarkSweepGC -XX:+UseCompressedOops -XX:+DoEscapeAnalysis -Xmx1G -cp "$(lein classpath)" clojure.main bench.clj 

