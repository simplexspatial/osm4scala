# osm4scala

[![Build Status](https://travis-ci.org/angelcervera/osm4scala.svg)](https://travis-ci.org/angelcervera/osm4scala)
[![Coverage Status](https://coveralls.io/repos/github/angelcervera/osm4scala/badge.svg?branch=master)](https://coveralls.io/github/angelcervera/osm4scala?branch=master)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/angelcervera/osm4scala/master/LICENSE.md)
[ ![Download](https://api.bintray.com/packages/angelcervera/maven/osm4scala/images/download.svg?version=1.0) ](https://bintray.com/angelcervera/maven/osm4scala/1.0/link)

Scala library focus in parse PBF2 Open Street Map files as iterators.

At the moment, practically all Open Street Map data distribution are published using the osm pbf format because for publishing/distribution it is looking for size save. 
But because this format has been designed to achieve really good compression, it is really complex to obtain an optimized way to process its content with moderns big data tools.

## Goal
With this library, you can forget about complexity of the osm.obf format and think about a **scala iterators of primitives** (nodes, ways and relations) or blob blocks.

For example, count all node primitives in a file is so simple as:
```scala
EntityIterator.fromPbf(inputStream).count(_.osmModel == OSMTypes.Node)
```

## Performance
The performance of the first version looks really good. You can find a resume in the section [Concurrent examples comparision](#concurrent-examples-comparision)

To have more representative permormance metrics, all metrics in this section are using only one thread.

For example, it expends only **32 seconds to iterate over near of 70 millions** of elements that compose Spain. 
Below the result of few executions of the [Primitives Counter Example](examples/counter/src/main/scala/com/acervera/osm4scala/examples/counter/Counter.scala) available in the code.
~~~~
Found [67,976,861] primitives in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 32.44 sec.
Found [4,839,505] primitives of type [Way] in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 31.72 sec.
Found [63,006,432] primitives of type [Node] in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 32.70 sec.
Found [130,924] primitives of type [Relation] in /home/angelcervera/projects/osm/spain-latest.osm.pbf in 32.66 sec.
~~~~

Other example, iterate over the **full planet (near of 4,000 millions of elements), 40 minutes**, reading the 36GB file from an USB3 drive.
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

Specs of the computer (laptop) used to execute the test:
```
Ubuntu 16.04.1 LTS Desktop / 64bits
Intel(R) Core(TM) i7-4712HQ CPU @ 2.30GHz
2x8GiB SODIMM DDR3 Synchronous 1600 MHz (0.6 ns)
512GB SAMSUNG SSD
```

##  Examples:
In the project, there is a folder called "examples" with few simple examples.

### Counter (One thread) [Source](examples/counter/src/main/scala/com/acervera/osm4scala/examples/counter/Counter.scala) .

Count the number of primitives in a pbf file, with he possibility of filter by primitive type.

### Counter Parallel using Scala Future.traverse [Source](examples/counter-parallel/src/main/scala/com/acervera/osm4scala/examples/counterparallel/CounterParallel.scala).

Because the library implements different iterator to be able to iterate over blocks and entities, it is really simple to use it in a parallel way.

This example show how to process data in parallel, using only Scala Future.traverse

This is the simple way, but has a big problem: Futures.traverse create **sequentially** one Future per element in the Iterator and parallel is executing them. That means put all block in memory.
**This is ok if you have enough memory** (16GB is enough to manage all USA or Europe) but if you want process the full planet
or heavy memory consume process per block, you will need more than that (Check example with AKKA).

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

### Counter Parallel using AKKA [Source](examples/counter-akka/src/main/scala/com/acervera/osm4scala/examples/counterakka).

This example show how to process data in parallel, using AKKA

The implementation is not complex at all, but it is necessary a little bit (a really little bit) of knowledge about AKKA to understand it.
Two big advantage respect the Future.traverse version:
- The memory used depends directly of the number of actor used, so you can process the full planet with no more of few GB of RAM.
- It is possible distribute the execution in different nodes.


### Concurrent examples comparision.
#### Ireland and North Ireland
- Entities: 15,751,251
- Counter (One thread): 8.91 sec.
- Concurrent Future.traverse: 5.31 sec.
- Concurrent AKKA 4 cores: 5.89 sec.

#### Spain
- Entities: 67,976,861
- Counter (One thread): 35.67 sec.
- Concurrent Future.traverse: 17.33 sec.
- Concurrent AKKA 4 cores: 16.82 sec.

#### North America (USA and Canada)
- Entries: 944,721,636
- Counter (One thread): 514 sec. / 8.5 min.
- Concurrent Future.traverse: 211 sec. / 3.5  min. (-XX:-UseGCOverheadLimit -Xms14g)
- Concurrent AKKA 4 cores: 256.70 sec. / 4.27 min. -> **But only uses 4 cores and 128M of RAM**, so can play "Solitaire" while you wait.

### Tags extraction [Source](examples/tagsextraction/src/main/scala/com/acervera/osm4scala/examples/tagsextraction/TagExtraction.scala).

Extract a list of unique tags from a pbf file, with he possibility of filter by primitive type.
The list is stored in a file given as parameter.


## Requirements:

- Protobuf compiler (only if you want build the library):
    
    Every OS has a different installation process. Has been tested with version 2.6.1
    
    Install in Ubuntu:
    ```
    sudo apt-get install protobuf-compiler
    ```
- Import the library using maven or sbt.

    ```
    libraryDependencies += "com.acervera.osm4scala" %% "osm4scala-core" % "1.0"
    ```
- Add resolver (only if you have problems resolving dependencies without it):

    ```
    resolvers += "osm4scala repo" at "http://dl.bintray.com/angelcervera/maven"
    ``` 

## As reference:

  - PBF2 Documentation: http://wiki.openstreetmap.org/wiki/PBF_Format
  - PBF2 Java library: https://github.com/openstreetmap/osmosis/tree/master/osmosis-osm-binary
  - Download whole planet pbf files: http://free.nchc.org.tw/osm.planet/
  - Download country pbf files: http://download.geofabrik.de/index.html
  - Scala protocol buffer library: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc

## Libraries:

  - ScalaPB: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc
  
