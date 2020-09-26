# Spark examples

```
osm4scala-spark-utilities Example of utilities processing OSM Pbf files, using Spark and osm4scala
Usage: osm4scala-spark-utilities [counter|tag_keys] [options]

  --help                   prints this usage text
  -i, --input <file/files>
                           Input is a required pbf2 format set of files.
  -o, --output <path>      Output is a required path to store the result.
  -f, --outputFormat [csv, orc, parquet, etc.]
                           Format that spark will used to store the result.
  -c, --coalesce <coalesce>
                           Number of files that will generate. By default, is not going to join files.
Command: counter [options]
Primitives counter.
  -t, --type <type>        Primitive type [Node, Way, Relation] used to filter.
Command: tag_keys [options]
Tags extraction.
  -t, --type <type>        Primitive type [Node, Way, Relation] used to filter.
```

## Examples:

```shell script
bin/spark-submit \
    --packages 'com.github.scopt:scopt_2.12:3.7.1' \
    --jars 'spark-osm-pbf-shaded_2.12.jar' \
    --class com.acervera.osm4scala.examples.spark.Driver \
    "osm4scala-examples-spark-utilities_2.12-1.0.5-SNAPSHOT.jar" \
    counter \
    -i <pbf files path> \
    -o <output> \
    -c 1 \
    -f csv

bin/spark-submit \
    --packages 'com.github.scopt:scopt_2.12:3.7.1' \
    --jars 'spark-osm-pbf-shaded_2.12.jar' \
    --class com.acervera.osm4scala.examples.spark.Driver \
    "osm4scala-examples-spark-utilities_2.12-1.0.5-SNAPSHOT.jar" \
    tag_keys \
    -i <pbf files path> \
    -o <output> \
    -c 1 \
    -f csv
```