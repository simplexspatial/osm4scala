import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import ReleaseTransformations._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
lazy val commonSettings = Seq(
  organization := "com.acervera.osm4scala",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  // Bintray
  publishMavenStyle := true,
  bintrayRepository := "maven",
  bintrayPackage := "osm4scala",
  bintrayReleaseOnPublish := false,

  // Release
  releaseCrossBuild := true,
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
  ),

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

      // Bintray
      bintrayPackage := "osm4scala-core",

      PB.grpc := false,
      scalaSource in PB.protobufConfig := sourceDirectory.value / "pbf_generated",
      libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % "1.1.7"
      )
    )
  )

lazy val examplesCounter = Project(id = "examples-counter", base = file("examples/counter")).
  settings(commonSettings: _*).
  settings(
    Seq(
      name := "osm4scala-examples-counter",
      description := "Scala Open Street Map Pbf 2 parser. Examples / Counter",

      // Bintray
      bintrayPackage := "osm4scala-examples-counter",

      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.5.0"
      )
    )
  ).dependsOn("core")
