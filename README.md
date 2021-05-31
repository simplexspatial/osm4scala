# osm4scala

[![Build Status](https://travis-ci.com/simplexspatial/osm4scala.svg?branch=master)](https://travis-ci.com/simplexspatial/osm4scala)
[![Coverage Status](https://coveralls.io/repos/github/simplexspatial/osm4scala/badge.svg?branch=master)](https://coveralls.io/github/simplexspatial/osm4scala?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=simplexspatial_osm4scala&metric=alert_status)](https://sonarcloud.io/dashboard?id=simplexspatial_osm4scala)
[![Gitter](https://img.shields.io/gitter/room/osm4scala/talk.svg)](https://gitter.im/osm4scala/talk)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/angelcervera/osm4scala/master/LICENSE.md)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala?ref=badge_shield)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](https://github.com/simplexspatial/osm4scala/blob/master/code_of_conduct.md)

<p align="center">
  <img src="website/static/android-chrome-192x192.png">
</p>

High performance Scala library and Spark Polyglot (Scala, Python, SQL, etc.) connector for OpenStreetMap Pbf files.

## Documentation and site
Full documentation at https://simplexspatial.github.io/osm4scala/



## Dev information:
Reminder: Only '+' will trigger cross versions.

### Prepare environment
It's possible to develop using a Windows machine, but all documentation suppose that you are using Linux or Mac.

The only special requirement is to execute `sbt compile` to generate the protobuf source code.
```shell script
sbt compile
```

### Release process
The publication into Maven Central has been removed from the release process, so now there are few steps:
1. Release.
    ```shell script
    git checkout master
    sbt release
    ```
2. Publish into Maven Central.
   Info at [xerial/sbt-sonatype](https://github.com/xerial/sbt-sonatype#advanced-build-settings)
   After set the right credentials file at [`$HOME/.sbt/1.0/sonatype.sbt`](https://github.com/xerial/sbt-sonatype#homesbtsbt-version-013-or-10sonatypesbt):
    ```shell script
    git checkout v1.*.*
    sbt clean
    PATCH_211=false sbt +publishSigned
    PATCH_211=true sbt +publishSigned
    # In this point, tree target/sonatype-staging/ will show all artifacts to publish.
    sbt sonatypeBundleRelease
    ```
3. Publish documentation and site.
    ```bash
    git checkout v1.*.*
    cd website
    nvm use
    export GIT_USER=<username>; export USE_SSH=true; npm run deploy
    ```

## References.

### PBF information:
  - PBF2 Documentation: http://wiki.openstreetmap.org/wiki/PBF_Format
  - PBF2 Java library: https://github.com/openstreetmap/osmosis/tree/master/osmosis-osm-binary
  - Download whole planet pbf files: http://free.nchc.org.tw/osm.planet/
  - Download country pbf files: http://download.geofabrik.de/index.html
  - Scala protocol buffer library: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc
  - OSM primitives: https://wiki.openstreetmap.org/wiki/Elements
    -  Node: https://wiki.openstreetmap.org/wiki/Node
    -  Way: https://wiki.openstreetmap.org/wiki/Way
    -  Relations: https://wiki.openstreetmap.org/wiki/Relation
    -  Tags: https://wiki.openstreetmap.org/wiki/Tags

### third party OSS libraries:
  - ScalaPB: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc
