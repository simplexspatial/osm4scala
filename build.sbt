import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.{releaseCrossBuild, _}

// Dependencies
lazy val scalatestVersion = "3.0.8"
lazy val scalacheckVersion = "1.14.3"
lazy val commonIOVersion = "2.5"
lazy val logbackVersion = "1.1.7"
lazy val scoptVersion = "3.7.1"
lazy val akkaVersion = "2.5.31"

// Releases versions
lazy val scala213 = "2.13.2"
lazy val scala212 = "2.12.11"
lazy val scalaVersions = List(scala213, scala212)

lazy val commonSettings = Seq(
  crossScalaVersions := scalaVersions,
  organization := "com.acervera.osm4scala",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage in ThisBuild := Some(
    url(s"https://github.com/angelcervera/osm4scala")
  ),
  scmInfo in ThisBuild := Some(
    ScmInfo(
      url("https://github.com/angelcervera/osm4scala"),
      "scm:git:git://github.com/angelcervera/osm4scala.git",
      "scm:git:ssh://github.com:angelcervera/osm4scala.git"
    )
  ),
  developers in ThisBuild := List(
    Developer(
      "angelcervera",
      "Angel Cervera Claudio",
      "angelcervera@silyan.com",
      url("https://www.acervera.com")
    )
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",
    "commons-io" % "commons-io" % commonIOVersion % "test"
  )
)

lazy val disablingPublishingSettings =
  Seq(skip in publish := true, publishArtifact := false)

lazy val enablingPublishingSettings = Seq(
  publishArtifact := true, // Enable publish
  publishMavenStyle := true,
  publishArtifact in Test := false,
  // Bintray
  bintrayPackageLabels := Seq("scala", "osm", "openstreetmap"),
  bintrayRepository := "maven",
  bintrayVcsUrl := Some("https://github.com/angelcervera/osm4scala.git")
)

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
lazy val root = (project in file("."))
  .aggregate(
    core,
    commonUtilities,
    examplesCounter,
    examplesCounterParallel,
    examplesCounterAkka,
    examplesTagsExtraction,
    examplesPrimitivesExtraction
  )
  .settings(
    name := "osm4scala-root",
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true,
    // don't use sbt-release's cross facility
    releaseCrossBuild := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val core = Project(id = "core", base = file("core")).settings(
  commonSettings,
  enablingPublishingSettings,
  name := "osm4scala-core",
  description := "Scala Open Street Map Pbf 2 parser. Core",
  bintrayPackage := "osm4scala",
  coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*",
  PB.targets in Compile := Seq(
    scalapb.gen(grpc = false) -> (sourceManaged in Compile).value
  ),
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )
)

// Examples

lazy val commonUtilities = Project(
  id = "examples-common-utilities",
  base = file("examples/common-utilities")
).settings(
  commonSettings,
  skip in publish := true,
  name := "osm4scala-examples-common-utilities",
  description := "Utilities shared by all examples",
  libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
)

lazy val examplesCounter =
  Project(id = "examples-counter", base = file("examples/counter"))
    .settings(
      commonSettings,
      disablingPublishingSettings,
      name := "osm4scala-examples-counter",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala"
    )
    .dependsOn(core, commonUtilities)

lazy val examplesCounterParallel = Project(
  id = "examples-counter-parallel",
  base = file("examples/counter-parallel")
).settings(
    commonSettings,
    disablingPublishingSettings,
    name := "osm4scala-examples-counter-parallel",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel threads"
  )
  .dependsOn(core, commonUtilities)

lazy val examplesCounterAkka =
  Project(id = "examples-counter-akka", base = file("examples/counter-akka"))
    .settings(
      commonSettings,
      disablingPublishingSettings,
      name := "osm4scala-examples-counter-akka",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel with AKKA",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion
      )
    )
    .dependsOn(core, commonUtilities)

lazy val examplesTagsExtraction = Project(
  id = "examples-tag-extraction",
  base = file("examples/tagsextraction")
).settings(
    commonSettings,
    disablingPublishingSettings,
    name := "osm4scala-examples-tags-extraction",
    description := "Extract all unique tags from the selected primitive type (Way, Node, Relation or All) using osm4scala"
  )
  .dependsOn(core, commonUtilities)

lazy val examplesBlocksExtraction = Project(
  id = "examples-blocks-extraction",
  base = file("examples/blocksextraction")
).settings(
    commonSettings,
    disablingPublishingSettings,
    name := "osm4scala-examples-blocks-extraction",
    description := "Extract all blocks from the pbf into a folder using osm4scala."
  )
  .dependsOn(core, commonUtilities)

lazy val examplesPrimitivesExtraction = Project(
  id = "examples-primitives-extraction",
  base = file("examples/primitivesextraction")
).settings(
    commonSettings,
    disablingPublishingSettings,
    name := "osm4scala-examples-primitives-extraction",
    description := "Extract all primitives from the pbf into a folder using osm4scala."
  )
  .dependsOn(core, commonUtilities)
