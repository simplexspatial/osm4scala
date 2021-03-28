# osm4scala

[![Build Status](https://travis-ci.com/simplexspatial/osm4scala.svg?branch=master)](https://travis-ci.com/simplexspatial/osm4scala)
[![Coverage Status](https://coveralls.io/repos/github/simplexspatial/osm4scala/badge.svg?branch=master)](https://coveralls.io/github/simplexspatial/osm4scala?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=simplexspatial_osm4scala&metric=alert_status)](https://sonarcloud.io/dashboard?id=simplexspatial_osm4scala)
[![Gitter](https://img.shields.io/gitter/room/osm4scala/talk.svg)](https://gitter.im/osm4scala/talk)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/angelcervera/osm4scala/master/LICENSE.md)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala?ref=badge_shield)


**Scala library** and **Spark Connector** focus on parsing [PBF2 OpenStreetMap files](https://wiki.openstreetmap.org/wiki/PBF_Format) as iterators.

At the moment, practically all OpenStreetMap data distribution are published using the osm pbf format because for publishing/distribution it is looking for size save.
Because this format has been designed to achieve good compression, it is really complex to obtain an optimized way to process its content.

## Goals
This library achieves two different goals:
- [Core library](#core-library): High performance Scala library to read OSM Pbf files as iterators.
- [Spark Connector](#spark-connector): Polyglot ([Scala](#examples-from-spark-shell), [Python](#examples-from-pyspark),
  [SQL](#examples-from-spark-sql) or R) Spark connector to query OSM Pbf files.
  
## Question?
Please, use [Stackoverflow](https://stackoverflow.com) with the tag `[osm4scala]`. **I will response asap**.


## Selecting the right Version
It is important to choose the right version depending of your Scala version.

| osm4scala | Scalapb | Scala | Spark |
|:---------:|:------:|:-------:|:-----:|
| 1.0.7    | 0.9.7  | 2.11     | 2.4 |
| 1.0.7    | 0.10.2 | 2.12     | 2.4, 3.0 |
| 1.0.7    | 0.10.2 | 2.13     | NA |

For example,
- If you want to import the Spark Connector for Scala 2.11 and Spark 2.4: `com.acervera.osm4scala:osm4scala-spark2-shaded_2.11:1.0.7`
- If you want to import the Spark Connector for Scala 2.12 and Spark 2.4: `com.acervera.osm4scala:osm4scala-spark2-shaded_2.12:1.0.7`
- If you want to import the Spark Connector for Scala 2.12 and Spark 3.0: `com.acervera.osm4scala:osm4scala-spark3-shaded_2.12:1.0.7`


## Core library
With Osm4scala, you can forget about complexity of the `osm.pbf` format and think about a **scala iterators of primitives**
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

### Dependencies: [ ![Download Core](https://api.bintray.com/packages/angelcervera/maven/osm4scala-core/images/download.svg) ](https://bintray.com/angelcervera/maven/osm4scala-core/_latestVersion)
- Import the library using sbt.
    ```scala
    libraryDependencies += "com.acervera.osm4scala" %% "osm4scala-core" % "<version>"
    ```
- Import the library using maven.
    ```xml
    <dependency>
        <groupId>com.acervera.osm4scala</groupId>
        <artifactId>osm4scala-core_${scala-version}</artifactId>
        <version>${version}</version>
    </dependency>
    ```

- **Only if you have problems resolving dependencies without it**, add my bintray repo:
    ```scala
    resolvers += "osm4scala repo" at "http://dl.bintray.com/angelcervera/maven"
    ```

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


## Support
I'm doing my best, but keep in mind that this is an opensource project, developed for free.

If you need / want osm4scala (or Scala, BigData, Spark, Java, etc...) premium support, don't hesitate to [contact me and contract my services](https://www.acervera.com).  

For free community support, there are two channels:
- Stackoverflow using the tag [osm4scala](https://stackoverflow.com/questions/tagged/osm4scala): if it is a technical question.
  Please, try to be clear, adding simple example to reproduce the error or whatever the porblem is.
- [![Gitter](https://img.shields.io/gitter/room/osm4scala/talk.svg)](https://gitter.im/osm4scala/talk): if it is suggestion,
  generic question or anything that does not fit in the Stackoverflow scope.  


## Contributing
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](code_of_conduct.md)

First of all, if you arrived to this point, thanks for at least think about contributing!!!

### Way to contribute
- Spread the word. Maybe a project in Github with a post in your blog, with a link to [osm4scala repo](https://github.com/simplexspatial/osm4scala)
- Join [![Gitter](https://img.shields.io/gitter/room/osm4scala/talk.svg)](https://gitter.im/osm4scala/talk) group
- Response questions in [Stackoverflow](https://stackoverflow.com/). Stackoverflow will send you notifications if you are
  watching the [osm4scala](https://stackoverflow.com/questions/tagged/osm4scala) tag.
- Post or comment issues for bugs, new features, feedback, etc.
- Contributing with improvements or fixes using a Pull Request. Before to create the PR, please:
    - Discuss with the authors on an issue ticket prior to doing anything big.
    - I appreciate all contributions, and I understand that usually people do it for free, **like me**. But please,
      I don't want to spend all my free time doing code reviews. So keep this in mind and don't modify anything that
      is not necessary. On this way, I can keep focus in the change core, and review and ship asap. Example: If you modify
      one line of code, don't reformat the full file!!
    - Follow the style, naming and structure conventions of the rest of the project.
        - There is a `scalastyle` and a `scalafmt` config file in root of the repository. Use it.
        - Follow the official [Scala Style Guide](https://docs.scala-lang.org/style/)
        - Using Scala inference and the compiler optimization coming from there is good. So use it and specify typing only
          for public functions/properties to help the API user. If the developer cannot figure out the type in a short
          function (or short scope), then there is a problem with the code complexity, with the developer or with both.
        - Variable names should be self descriptive and camel case. `_` or others not alphabetical chars are not allowed
          if there is not a real reason.
        - If you find that repeating a function/property name is needed, something is wrong. It means that two function/property
          are used for the same thing, so you can remove one of them. Never try to fix it adding a prefix (like `_` or `$`)
    - One feature per PR. Don't modify things that are not related to the ticket and are not necessary for it.
    - If you find something wrong or that could be better, create a new ticket and fix it there (even if it is simple formatting style)
    - Make your commits atomics and easy to merge and review. Keep in mind that only "squash and rebase" is available in the PR.
    - Always apply KISS principle
    - No code duplication allowed at all. That is the first symptom of horrible design.
    - Try always functional approaches over imperative. Sometime is not possible, specially working with iterators over files
      (it means mutability and side effects) like in this project, but all around that should be as functional as possible.
    - Don't pollute the code with things that does not add **value**.
    - Avoid using other libraries as possible. This is a library used with other complex frameworks like Spark or toolkits
      like AKKA. Dependencies in these cases are source of problems.
    - Again, modify only the code that is strictly necessary and it's inside of the scope of the ticket. Currently, all stuff
      work with no problem and with a great performance, so it is ok (In fact, this is the point!).
    - If you think something could be better, great! Create a ticket with a good description (why, motivation, etc) and
      we can talk from there.
    - Don't write production code for testing or debugging. All related with testing should be with the testing source code. 
    - Verify that all tests are passing, for all Scala/Spark version available, even if you think that you did not touch anything.
      ```
      PATCH_211=false sbt clean +test
      PATCH_211=true sbt clean +test
      ```
    - I hope this long list did not scare you. 🙇

### Prepare environment
It's possible to develope using a Windows machine, but all documentation suppose that you are using Linux or Mac.

The only special requirement is to execute `sbt compile` to generate the protobuf source code.
```shell script
sbt compile
```


## Other notes
### As reference:
  - PBF2 Documentation: http://wiki.openstreetmap.org/wiki/PBF_Format
  - PBF2 Java library: https://github.com/openstreetmap/osmosis/tree/master/osmosis-osm-binary
  - Download whole planet pbf files: http://free.nchc.org.tw/osm.planet/
  - Download country pbf files: http://download.geofabrik.de/index.html
  - Scala protocol buffer library: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc

### Libraries:
  - ScalaPB: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc

### Release process
```shell script
git checkout master
PATCH_211=false sbt release

git checkout v1.*.*
PATCH_211=true sbt clean +publish
```

