# osm

A clojure library and CLI to work with Open Street Map (OSM) data, turning it into GeoJSON.

_work in progress_

## Usage

As a general rule, you can work with both the bz2(recommended) or the raw xml as input files.

The functions work emitting GeoJSON individual features for each point (node, in osm terms) and polygon/line (way, in osm terms).

```json
{
  "type":"Feature",
  "properties": {"timestamp":"xxx","tag1":"value","tag2":"value"},
  "id":"123",
  "geometry": {
    "type":"Point",
    "coordinates": [10.10,20.20]
  }
}
```

There are two ways to operate: Fully in RAM or swapping to disk. 

In RAM (default) is recommended for smaller datasets. How small depends on available RAM.

For bigger data, or if you are in doubt, you can use the swapping to disk version. This will use a compact temporary LevelDB (deleted at the end) at your temporary folder to hold some temporary data. This will use about 1.5x of the bz2 size or 0.11x the raw xml size. 

The time depends a lot on the machine:

On my Samsung Chromebook 2, with an Intel Celeron dual core and an eMMC disk it takes 50+ minutes to process a 380MB bz2 of Brazil data with 3,740,964 features, taking less them 100MB of RAM.

On my desktop, with an Intel i5 quad-core and nice Sata3 7200RPM, it takes 20- minutes for a 473MB bz2.

### Library

With leningen:

[![Clojars Project](https://img.shields.io/clojars/v/osm.svg)](https://clojars.org/osm)

And require the functions:

```clojure
  (:require [osm.reader :as r] [osm.writer :as w])
```

You can use both the Bz2 (recommended) as input or the raw xml file.

For smaller data, you can work all on RAM:

```clojure 
  (r/read-stream "osm.xml.bz2" 
   (fn [feature] 
    (println feature)))
```

For bigger data, the functions can use to the disk to avoid running out of RAM:

```clojure
  (r/read-stream-swap "osm.xml.bz2"
   (function [feature]
     (println feature)))
```

There is one function to write each geojson to files in a dir:

```clojure
  (spit-each "osm.xml.bz2" "dir")
  (spit-each-swap "osm.xml.bz2" "dir")
```

Or write all to a single geojson FeatureCollection, not recommended on big files but swapping is available (but still not recomeded).

```clojure
  (w/spit-all "osm.xml.bz2" "osm.geojson")
  (w/spit-all-swap "osm.xml.bz2" "osm.geojson")
```

There is also a, more experimental, feature to send the data to a web hook.

This will send the data as  a featurecollection of up to 512 features.

```clojure
    (post "osm.xml.bz2" "http://localhost:8080/myhook")
```

### Command Line

Download the release jar *TODO*:

```shell
  $ java -jar osm.jar --help
```

## License

MIT

