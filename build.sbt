import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import ReleaseTransformations._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*"

//organization := "com.acervera.osm4scala"
//organizationHomepage := Some(url("http://www.acervera.com"))
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
//description := "Scala Open Street Map Pbf 2 parser."

bintrayReleaseOnPublish in ThisBuild := false

lazy val core = (project in file("core")).
  settings(
    PB.protobufSettings ++ Seq(
      organization := "com.acervera.osm4scala",
      name := "osm4scala-core",
      organizationHomepage := Some(url("http://www.acervera.com")),
      licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
      description := "Scala Open Street Map Pbf 2 parser. Core",
      scalaVersion := "2.11.8",
      crossScalaVersions := Seq("2.10.6", "2.11.8"),
      // Bintray
      publishMavenStyle := true,
      bintrayRepository := "maven",
      bintrayPackage := "osm4scala-core",
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
      PB.grpc := false,
      scalaSource in PB.protobufConfig := sourceDirectory.value / "pbf_generated",
      libraryDependencies ++= List(
        "commons-io" % "commons-io" % "2.5" % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.7",
        "org.scalatest" %% "scalatest" % "2.2.6" % "test",
        "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
      )
    )
  )
