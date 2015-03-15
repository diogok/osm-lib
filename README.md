# osm

A clojure library and CLI to work with Open Street Map (OSM) data, turning it into GeoJSON.

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

There is also two ways to operate: Fully in RAM and swapping to disk. 

In RAM (default) is recommended for smaller datasets. How small depends on available RAM, but on with 1GB you can process about a XML of about xxGB or circa xxMB bz2.

For bigger data, or if you are in doubt, you can use the swapping to disk version. This will use a compact temporary LevelDB (deleted at the end) at your temporary folder to hold some temporary data. This will use about 1.5x of the bz2 size or 0.11x the raw xml size. 

The time depends a lot on the harddrive, on my regular spining notebook harddrive it takes xx minutes, using a 600MB DB and less them 1GB RAM to process brazil.osm, reading a 400MB bz2 (5.2GB xml).

### Library

With leningen:

```clojure
  [diogok/osm "0.0.1"]
```

And require the functions:

```clojure
  (:require [osm.reader :as r] [osm.writer :as w])
```

You can use both the Bz2 (recommended) as input and the raw xml file.

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

Also some webhooks:

```clojure
  (w/web "osm.xml.bz2" 
   { ;; available all clj-http options
    :url "http://domain.com/uri"
    :method "POST" ;; only POST or PUT, and PUT will append /id on url. Default to POST.
    :bulk true ;; If should send in groups (good for performance). Defaul to false.
    :bulk-type :collection ;; Type fo bulk. :collection to FeatureCollection, :prop to use a property and an array of features and :raw to send just to array. Default to :raw.
    :bulk-prop :docs ;; if :bulk-type :prop, the property to put data under. Default to :docs.
    :bulk-size 500 ;; Size of bulk request. Default to 500.
    :pre fn ;; pass each feature on a function before sending. Default to nil.
   })

  (w/couchdb "osm.xml.bz2" "http://cloudant.com/your_db") ;; alias to bulk into couchdb
```

### Command Line

Soon.

## License

MIT

