#/bin/bash

#[[ ! -e "data/brazil.osm.bz2" ]] && mkdir -p data && wget http://download.geofabrik.de/south-america/brazil-latest.osm.bz2 -O data/brazil-latest.osm.bz2

time java -server -XX:+UseConcMarkSweepGC -XX:+UseCompressedOops -XX:+DoEscapeAnalysis -Xmx1G -cp "$(lein classpath)" clojure.main bench.clj 

