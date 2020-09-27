# osm4scala

[![Build Status](https://travis-ci.org/angelcervera/osm4scala.svg?branch=master)](https://travis-ci.org/angelcervera/osm4scala)
[![Coverage Status](https://coveralls.io/repos/github/angelcervera/osm4scala/badge.svg?branch=master)](https://coveralls.io/github/angelcervera/osm4scala?branch=master)
[ ![Download](https://api.bintray.com/packages/angelcervera/maven/osm4scala/images/download.svg) ](https://bintray.com/angelcervera/maven/osm4scala/_latestVersion)
[![Gitter](https://img.shields.io/gitter/room/osm4scala/talk.svg)](https://gitter.im/osm4scala/talk)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/angelcervera/osm4scala/master/LICENSE.md)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala?ref=badge_shield)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](code_of_conduct.md)

**Scala library** and **Spark Connector** focus on parsing PBF2 OpenStreetMap files as iterators.

At the moment, practically all OpenStreetMap data distribution are published using the osm pbf format because for publishing/distribution it is looking for size save. 
Because this format has been designed to achieve good compression, it is really complex to obtain an optimized way to process its content.

## Goals
This library achieves two different goals:
- [Core library](#core-library): High performance Scala library to read OSM Pbf files as iterators.
- [Spark Connector](#spark-connector): Polyglot (Scala, Python, SQL or R) Spark connector to query OSM Pbf files.

## Core library
With Osm4scala, you can forget about complexity of the osm.obf format and think about a **scala iterators of primitives**
(nodes, ways and relations) or blob blocks.

For example, counting all node primitives in a file is so simple as:
```scala
EntityIterator.fromPbf(inputStream).count(_.osmModel == OSMTypes.Node)
```

This is a simple example, but because the iterator nature, you can use it for more complex tasks and patterns,
to process data at the same time you are reading with near zero memory usage, for example.

The library allows to read the pbf file on two different ways:
- Entity per entity as an `EntityIterator`, using any of the `EntityIterator` factories. This method allows you to iterate
  over `OSMEntity` trait objects, that could be any of the following: `NodeEntity`, `WayEntity` or `WayEntity` 
- Blob per blob as an `BlobIterator`, from any of the `BlobIterator` factories.

### Dependencies
- Import the library using sbt.
    ```
    libraryDependencies += "com.acervera.osm4scala" %% "osm4scala-core" % "<version>"
    ```
- Import the library using maven.
    ```
    <dependency>
        <groupId>com.acervera.osm4scala</groupId>
        <artifactId>osm4scala-core_${scala-version}</artifactId>
        <version>${version}</version>
    </dependency>
    ```

- **Only if you have problems resolving dependencies without it**, add my bintray repo:
    ```
    resolvers += "osm4scala repo" at "http://dl.bintray.com/angelcervera/maven"
    ```

## Spark Connector

With the Osm4scala Spark connector, is possible to read OSM Pbf file from Spark, Python, SQL or R, using the format name **"osm.pbf"**
So nothing weird!

### Schema
Next, the schema exposed:
```
StructType(
    StructField(id,LongType,false),
    StructField(type,ByteType,false),
    StructField(latitude,DoubleType,true),
    StructField(longitude,DoubleType,true),
    StructField(nodes,ArrayType(LongType,true),true),
    StructField(relations,ArrayType(StructType(
        StructField(id,LongType,true),
        StructField(relationType,ByteType,true),
        StructField(role,StringType,true)
    ),true),true),
    StructField(tags,MapType(StringType,StringType,true),true)
)
```

### Examples from spark-shell:

1. Start the shell:
    ```shell script
    bin/spark-shell --packages 'com.acervera.osm4scala:osm4scala-spark-shaded_2.12:1.0.5'
    ```
2. Load the data set and execute queries:
    ```scala
    scala> val osmDF = spark.sqlContext.read.format("osm.pbf").load("<osm files path here>")
    osmDF: org.apache.spark.sql.DataFrame = [id: bigint, type: tinyint ... 5 more fields]
    
    scala> osmDF.createOrReplaceTempView("osm")
    
    scala> spark.sql("select type, count(*) as num_primitives from osm group by type").show()
    +----+--------------+                                                           
    |type|num_primitives|
    +----+--------------+
    |   1|        338795|
    |   2|         10357|
    |   0|       2328075|
    +----+--------------+
    
    scala> spark.sql("select distinct(explode(map_keys(tags))) as tag_key from osm order by tag_key asc").show()
    +------------------+                                                            
    |           tag_key|
    +------------------+
    |             Calle|
    |        Conference|
    |             Exper|
    |             FIXME|
    |         ISO3166-1|
    |  ISO3166-1:alpha2|
    |  ISO3166-1:alpha3|
    | ISO3166-1:numeric|
    |         ISO3166-2|
    |           MAC_dec|
    |            Nombre|
    |            Numero|
    |              Open|
    |        Peluqueria|
    |    Residencia UEM|
    |          Telefono|
    |         abandoned|
    | abandoned:amenity|
    | abandoned:barrier|
    |abandoned:building|
    +------------------+
    only showing top 20 rows
    
    scala> spark.sql("select id, latitude, longitude, tags from osm where type = 0").show()
    +--------+------------------+-------------------+--------------------+
    |      id|          latitude|          longitude|                tags|
    +--------+------------------+-------------------+--------------------+
    |  171933|          40.42006|-3.7016600000000004|                  []|
    |  171946|          40.42125|-3.6844500000000004|[highway -> traff...|
    |  171948|40.420230000000004|-3.6877900000000006|                  []|
    |  171951|40.417350000000006|-3.6889800000000004|                  []|
    |  171952|          40.41499|-3.6889800000000004|                  []|
    |  171953|          40.41277|-3.6889000000000003|                  []|
    |  171954|          40.40946|-3.6887900000000005|                  []|
    |  171959|          40.40326|-3.7012200000000006|                  []|
    |20952874|          40.42099|-3.6019200000000007|                  []|
    |20952875|40.422610000000006|-3.5994900000000007|                  []|
    |20952878| 40.42136000000001| -3.601470000000001|                  []|
    |20952879| 40.42262000000001| -3.599770000000001|                  []|
    |20952881| 40.42905000000001|-3.5970500000000007|                  []|
    |20952883| 40.43131000000001|-3.5961000000000007|                  []|
    |20952888| 40.42930000000001| -3.596590000000001|                  []|
    |20952890| 40.43012000000001|-3.5961500000000006|                  []|
    |20952891| 40.43043000000001|-3.5963600000000007|                  []|
    |20952892| 40.43057000000001|-3.5969100000000007|                  []|
    |20952893| 40.43039000000001|-3.5973200000000007|                  []|
    |20952895| 40.42967000000001|-3.5972300000000006|                  []|
    +--------+------------------+-------------------+--------------------+
    only showing top 20 rows
    
    
    scala> spark.sql("select id, nodes, tags from osm where type = 1").show()
    +-------+--------------------+--------------------+                             
    |     id|               nodes|                tags|
    +-------+--------------------+--------------------+
    |3996189|[23002322, 230022...|[name -> M-40, in...|
    |3996190|[20952892, 213645...|[name -> Avenida ...|
    |3996191|[21364526, 253693...|[lanes -> 2, onew...|
    |3996192|[20952914, 242495...|[name -> Plaza de...|
    |3996195|[20952923, 421448...|[name -> Calle de...|
    |3996196|[20952942, 209529...|[name -> Avenida ...|
    |3996197|[20952893, 209628...|[name -> Avenida ...|
    |3996199|[20952929, 209529...|[name -> Calle de...|
    |3996203|[20952948, 391553...|[name -> Calle de...|
    |3997425|[20960686, 219912...|[name -> Avenida ...|
    |3997426|[2424952617, 2095...|[name -> Avenida ...|
    |3997427|[20960717, 209606...|[name -> Calle de...|
    |3997428|[20960693, 209607...|[highway -> terti...|
    |3997429|[20960696, 421448...|[name -> Calle de...|
    |3997430|[20963025, 209630...|[name -> Paseo de...|
    |3997432|[20960688, 209607...|[name -> Calle de...|
    |3997433|[1811010970, 1811...|[name -> Calle de...|
    |4004278|[255148257, 21067...|[name -> Calle de...|
    |4004280|[20963101, 209630...|[name -> Calle de...|
    |4004281|[25530614, 297977...|[name -> Calle de...|
    +-------+--------------------+--------------------+
    only showing top 20 rows
    
    
    scala> spark.sql("select id, relations, tags from osm where type = 2").show()
    +-----+--------------------+--------------------+                               
    |   id|           relations|                tags|
    +-----+--------------------+--------------------+
    |11331|[[2609596233, 0, ...|[network -> Cerca...|
    |11332|[[196618381, 1, p...|[network -> Cerca...|
    |14612|[[24698019, 1, ou...|[website -> http:...|
    |30117|[[26629303, 1, ou...|[type -> multipol...|
    |30399|[[307006515, 1, i...|[website -> http:...|
    |38757|[[6120746, 1, ], ...|[network -> lcn, ...|
    |38965|[[44571128, 1, fr...|[type -> restrict...|
    |48292|[[317775809, 0, s...|[network -> Metro...|
    |49958|[[308868559, 0, v...|[type -> restrict...|
    |49959|[[308868558, 0, v...|[type -> restrict...|
    |50874|[[26141446, 1, ou...|[name -> Escuela ...|
    |52312|[[24531942, 1, ou...|[name -> Pista pr...|
    |52313|[[24698560, 1, ou...|[type -> multipol...|
    |53157|[[2609596077, 0, ...|[network -> Cerca...|
    |55085|[[246285922, 0, s...|[network -> Cerca...|
    |55087|[[194005015, 1, ]...|[network -> Cerca...|
    |55799|[[28775036, 1, ou...|[type -> multipol...|
    |56044|[[258556530, 0, s...|[network -> Metro...|
    |56260|[[144383571, 1, o...|[name -> Ayuntami...|
    |56791|[[32218973, 0, st...|[network -> Metro...|
    +-----+--------------------+--------------------+
    only showing top 20 rows
    
    ```

### Examples from spark-sql
1. Start the shell:
    ```shell script
    bin/spark-sql --packages 'com.acervera.osm4scala:osm4scala-spark-shaded_2.12:1.0.5'
    ```
2. Load the data set and execute queries:
    ``` sql
    spark-sql> CREATE TABLE osm USING osm.pbf LOCATION '<osm files path here>'
    spark-sql> select id, latitude, longitude, tags from osm where type = 0 limit 20
    171933	40.42006	-3.7016600000000004	{}
    171946	40.42125	-3.6844500000000004	{"crossing":"traffic_signals","crossing_ref":"zebra","highway":"traffic_signals"}
    171948	40.420230000000004	-3.6877900000000006	{}
    171951	40.417350000000006	-3.6889800000000004	{}
    171952	40.41499	-3.6889800000000004	{}
    171953	40.41277	-3.6889000000000003	{}
    171954	40.40946	-3.6887900000000005	{}
    171959	40.40326	-3.7012200000000006	{}
    20952874	40.42099	-3.6019200000000007	{}
    20952875	40.422610000000006	-3.5994900000000007	{}
    20952878	40.42136000000001	-3.601470000000001	{}
    20952879	40.42262000000001	-3.599770000000001	{}
    20952881	40.42905000000001	-3.5970500000000007	{}
    20952883	40.43131000000001	-3.5961000000000007	{}
    20952888	40.42930000000001	-3.596590000000001	{}
    20952890	40.43012000000001	-3.5961500000000006	{}
    20952891	40.43043000000001	-3.5963600000000007	{}
    20952892	40.43057000000001	-3.5969100000000007	{}
    20952893	40.43039000000001	-3.5973200000000007	{}
    20952895	40.42967000000001	-3.5972300000000006	{}
    ```

### Examples from pyspark
1. Start the shell:
    ```shell script
    bin/pyspark --packages 'com.acervera.osm4scala:osm4scala-spark-shaded_2.12:1.0.5'
    ```
2. Load the data set and execute queries:
    ```python
    >>> osmDF = spark.read.format("osm.pbf").load("<osm files path here>")
    >>> osmDF
    DataFrame[id: bigint, type: tinyint, latitude: double, longitude: double, nodes: array<bigint>, relations: array<struct<id:bigint,relationType:tinyint,role:string>>, tags: map<string,string>]
    >>> osmDF.createOrReplaceTempView("osm")
    >>> spark.sql("select type, count(*) as num_primitives from osm group by type").show()
    +----+--------------+                                                           
    |type|num_primitives|
    +----+--------------+
    |   1|        338795|
    |   2|         10357|
    |   0|       2328075|
    +----+--------------+
    ```

### Dependencies
The simplest way to add the library to the job, is using the shaded flat jar.
- Import the library using maven or sbt.
    ```
    libraryDependencies += "com.acervera.osm4scala" %% "osm4scala-spark-shaded" % "<version>"
    ```
- Add the resolver **only if you have problems resolving dependencies without it**:
    ```
    resolvers += "osm4scala repo" at "http://dl.bintray.com/angelcervera/maven"
    ```
  
For example:
- Submitting a job:
    ```shell script
    bin/spark-submit --packages 'com.acervera.osm4scala:osm4scala-spark-shaded_2.12:1.0.5' .....
    ```

- Using in a Spark shell:

  
- Using in a Spark SQL shell:

  
- Uni
bin/pyspark --packages 'com.acervera.osm4scala:osm4scala-spark-shaded_2.12:1.0.5-SNAPSHOT'


> Why osm4scala-spark-shaded is available as fat shaded library?
>
> osm4scala has a transitive dependency with Java Google Protobuf library. Spark, Hadoop and other libraries in the
> ecosystem are using an old version of the same library (currently v2.5.0 from Mar, 2013) that is not compatible.
>
> To solve the conflict, I published the library in to fashion:
> - Fat and Shaded as `osm4scala-spark-shaded` that solves `com.google.protobuf.**` conflicts.
> - Don't shaded as `osm4scala-spark`, so you can solve the conflict on your way.
>




## Selecting the right Versions
It is important to choose the right version depending of your Scala version.

| osm4scala | Scala | Scalapb | Spark |
|:--------:|:------:|:-------:|:-----:|
| 1.0.5 | 2.12, 2.13 | 0.10.2 | 3.0.* |
| 1.0.4 | 2.12, 2.13 | 0.10.2 | NA |
| 1.0.3 | 2.11, 2.12, 2.13 | 0.9.7 | NA |
| 1.0.1 | 2.11, 2.12 | 0.5.47 | NA |
| 1.0 | 2.10, 2.11 | 0.5.47 | NA |

> Why osm4scala is not available for all Scala version?
>
> osm4scala use ScalaPB library for Google Protobuf parsing. This library is not supporting Scala 2.11 in the more recent
> versions. Branch 1.0.3 will be update with all new functionalities, like latest 1.0.* version.
>  




## Performance
The performance of the first version looks really good. You can find a resume in the section [Concurrent examples comparision](#concurrent-examples-comparision)

Laptop specifications used to execute testing and performance comparison:
```
Ubuntu 16.04.1 LTS Desktop / 64bits
Intel(R) Core(TM) i7-4712HQ CPU @ 2.30GHz
2x8GiB SODIMM DDR3 Synchronous 1600 MHz (0.6 ns)
512GB SAMSUNG SSD
```

### One thread performance.

To have more representative performance metrics, all metrics in this section are using only one thread.

For example, it expends only **32 seconds to iterate over near of 70 millions** of elements that compose Spain. 
Below the result of few executions of the [Primitives Counter Example](examples/counter/src/main/scala/com/acervera/osm4scala/examples/counter/Counter.scala) available in the code.
~~~~
Found [67,976,861] primitives in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 32.44 sec.
Found [4,839,505] primitives of type [Way] in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 31.72 sec.
Found [63,006,432] primitives of type [Node] in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 32.70 sec.
Found [130,924] primitives of type [Relation] in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 32.66 sec.
~~~~

Other example, iterate over the **full planet (near of 4,000 millions of elements on August 2016), 40 minutes**, reading the 36GB file from an USB3 drive.
~~~
Found [3,976,885,170] primitives in /media/angelcervera/My Passport/osm/planet-latest.osm.pbf in 2,566.11 sec.
~~~

The other example, [Tag Extraction Example](examples/tagsextraction/src/main/scala/com/acervera/osm4scala/examples/tagsextraction/TagExtraction.scala)
expends only 42 seconds to extract the list of all unique tags from the Spain pbf.
~~~~
Found [4,166] different tags in /home/angelcervera/projects/osm/spain-latest.osm.pbf. List stored in /home/angelcervera/projects/osm/spain-latest.tags.txt. Time to process: 39.22 sec.
Found [2,451] different tags in primitives of type [Way] in /home/angelcervera/projects/osm/spain-latest.osm.pbf. List stored in /home/angelcervera/projects/osm/spain-latest.tags.txt. Time to process: 33.47 sec.
~~~~

And the use of memory is negligible.
All previous performance  metrics are using using a **single thread**. Check examples bellow for parallel processing.

###  Examples:
In the project, there is a folder called "examples" with few simple examples.

#### Counter (One thread) [Source](examples/counter/src/main/scala/com/acervera/osm4scala/examples/counter/Counter.scala) .

Count the number of primitives in a pbf file, with he possibility of filter by primitive type.

#### Counter Parallel using Scala Future.traverse [Source](examples/counter-parallel/src/main/scala/com/acervera/osm4scala/examples/counterparallel/CounterParallel.scala).

Because the library implements different iterator to be able to iterate over blocks and entities, it is really simple to use it in a parallel way.

This example show how to process data in parallel, using only Scala Future.traverse

This is the simple way, but has a big problem: Futures.traverse create **sequentially** one Future per element in the
Iterator and parallel is executing them. That means put all block in memory.
**This is ok if you have enough memory** (16GB is enough to manage all USA or Europe) but if you want process the full
planet or a heavy memory consume process per block, you will need more than that (Check example with AKKA).

~~~scala
  val counter = new AtomicLong()
  def count(pbfIS: InputStream): Long = {
    val result = Future.traverse(BlobTupleIterator.fromPbf(pbfIS))(tuple => Future {
      counter.addAndGet( count(tuple._2) )
    })
    Await.result(result, Duration.Inf)
    counter.longValue()
  }
~~~

#### Counter Parallel using AKKA [Source](examples/counter-akka/src/main/scala/com/acervera/osm4scala/examples/counterakka).

This example show how to process data in parallel, using AKKA

The implementation is not complex at all, but it is necessary a little bit (a really little bit) of knowledge about AKKA to understand it.
Two big advantage respect the Future.traverse version:
- The memory used depends directly on the number of actor used, so you can process the full planet with no more of few GB of RAM.
- It is possible distribute the execution in different nodes.


#### Counter Concurrent examples comparison.

##### Ireland and North Ireland
- Entities: 15,751,251
- Counter (One thread): 8.91 sec.
- Concurrent Future.traverse: 5.31 sec.
- Concurrent AKKA 4 cores: 5.89 sec.

##### Spain
- Entities: 67,976,861
- Counter (One thread): 35.67 sec.
- Concurrent Future.traverse: 17.33 sec.
- Concurrent AKKA 4 cores: 16.82 sec.

##### North America (USA and Canada)
- Entities: 944,721,636
- Counter (One thread): 514 sec. / 8.5 min.
- Concurrent Future.traverse: 211 sec. / 3.5  min. (-XX:-UseGCOverheadLimit -Xms14g)
- Concurrent AKKA 4 cores: 256.70 sec. / 4.27 min. -> **But only uses 4 cores and 128M of RAM**, so can play "Solitaire" while you wait.

#### Tags extraction [Source](examples/tagsextraction/src/main/scala/com/acervera/osm4scala/examples/tagsextraction/TagExtraction.scala).

Extract a list of unique tags from an osm.pbf file, optionally filtering by primitive type.
The list is stored in a file given as parameter.


## Requirements:

- Protobuf compiler (only if you want build the library):
    
    Every OS has a different installation process. Has been tested with version 2.6.1
    
    Install in Ubuntu:
    ```
    sudo apt-get install protobuf-compiler
    ```

## As reference:

  - PBF2 Documentation: http://wiki.openstreetmap.org/wiki/PBF_Format
  - PBF2 Java library: https://github.com/openstreetmap/osmosis/tree/master/osmosis-osm-binary
  - Download whole planet pbf files: http://free.nchc.org.tw/osm.planet/
  - Download country pbf files: http://download.geofabrik.de/index.html
  - Scala protocol buffer library: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc

## Libraries:

  - ScalaPB: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc
  
