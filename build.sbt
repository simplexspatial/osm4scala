import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import ReleaseTransformations._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

publishArtifact := false // Avoid publish default artifact

// Release
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

// Bintray BUG workaround: https://github.com/softprops/bintray-sbt/issues/93
bintrayRelease := false
bintrayEnsureBintrayPackageExists := false
bintrayEnsureLicenses := false

lazy val commonSettings = Seq(
  organization := "com.acervera.osm4scala",
  scalaVersion := "2.11.8",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  // Bintray
  publishArtifact := true,
  publishMavenStyle := true,
  bintrayRepository := "maven",
  bintrayPackage := "osm4scala",
  bintrayReleaseOnPublish := false,

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
    "commons-io" % "commons-io" % "2.5" % "test"
  )

)

lazy val core = Project(id = "core", base = file("core")).
  settings(commonSettings: _*).
  settings(
    PB.protobufSettings ++ Seq(
      name := "osm4scala-core",
      description := "Scala Open Street Map Pbf 2 parser. Core",
      coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*",
      PB.grpc := false,
      scalaSource in PB.protobufConfig := sourceDirectory.value / "pbf_generated",
      libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % "1.1.7"
      )
    )
  )

lazy val commonUtilities = Project(id = "examples-common-utilities", base = file("examples/common-utilities")).
  settings(commonSettings: _*).
  settings(
    Seq(
      name := "osm4scala-examples-common-utilities",
      description := "Utilities shared by all examples",
      publishArtifact := false // Don't publish this example in maven. Only the library.
    )
  )

lazy val examplesCounter = Project(id = "examples-counter", base = file("examples/counter")).
  settings(commonSettings: _*).
  settings(
    Seq(
      name := "osm4scala-examples-counter",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala",
      publishArtifact := false, // Don't publish this example in maven. Only the library.
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.5.0"
      )
    )
  ).dependsOn("core", "examples-common-utilities")

lazy val examplesTagsExtraction = Project(id = "examples-tag-extraction", base = file("examples/tagsextraction")).
  settings(commonSettings: _*).
  settings(
    Seq(
      name := "osm4scala-examples-tags-extraction",
      description := "Extract all unique tags from the selected primitive type (Way, Node, Relation or All) using osm4scala",
      publishArtifact := false, // Don't publish this example in maven. Only the library.
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.5.0"
      )
    )
  ).dependsOn("core", "examples-common-utilities")

