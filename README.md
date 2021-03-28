# osm4scala

[![Build Status](https://travis-ci.com/simplexspatial/osm4scala.svg?branch=master)](https://travis-ci.com/simplexspatial/osm4scala)
[![Coverage Status](https://coveralls.io/repos/github/simplexspatial/osm4scala/badge.svg?branch=master)](https://coveralls.io/github/simplexspatial/osm4scala?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=simplexspatial_osm4scala&metric=alert_status)](https://sonarcloud.io/dashboard?id=simplexspatial_osm4scala)
[![Gitter](https://img.shields.io/gitter/room/osm4scala/talk.svg)](https://gitter.im/osm4scala/talk)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/angelcervera/osm4scala/master/LICENSE.md)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fangelcervera%2Fosm4scala?ref=badge_shield)

Documentation at ???????????



### Prepare environment
It's possible to develop using a Windows machine, but all documentation suppose that you are using Linux or Mac.

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

### third party OSS libraries:
  - ScalaPB: https://scalapb.github.io/ and https://github.com/thesamet/sbt-protoc

### Release process
```shell script
git checkout master
PATCH_211=false sbt release

git checkout v1.*.*
PATCH_211=true sbt clean +publish
```

