import ReleaseTransformations._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

publishArtifact := false // Avoid publish default artifact

// Release
crossScalaVersions := Seq(/*"2.10.6",*/ "2.11.11", "2.12.2")
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
  scalaVersion := "2.12.2",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  publishArtifact := true, // Enable publish
  publishMavenStyle := true,
  publishArtifact in Test := false, // No publish test stuff
  pomExtra :=
    <url>https://github.com/angelcervera/osm4scala</url>
      <scm>
        <connection>scm:git:git://github.com/angelcervera/osm4scala.git</connection>
        <developerConnection>scm:git:ssh://git@github.com/angelcervera/osm4scala.git</developerConnection>
        <url>https://github.com/angelcervera/osm4scala</url>
      </scm>
      <developers>
        <developer>
          <id>angelcervera</id>
          <name>Angel Cervera Claudio</name>
          <email>angelcervera@silyan.com</email>
        </developer>
      </developers>
  ,

  // Bintray
  bintrayRepository := "maven",
  bintrayPackage := "osm4scala",
  bintrayReleaseOnPublish := false,
  bintrayRelease := false,

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "commons-io" % "commons-io" % "2.5" % "test"
  )

)

lazy val core = Project(id = "core", base = file("core")).
  settings(
    commonSettings,
    name := "osm4scala-core",
    description := "Scala Open Street Map Pbf 2 parser. Core",
    coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*",
    PB.targets in Compile := Seq(
      scalapb.gen(grpc=false) -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.7"
    )
  )







// Examples

lazy val commonUtilities = Project(id = "examples-common-utilities", base = file("examples/common-utilities")).
  settings(
    commonSettings,
    name := "osm4scala-examples-common-utilities",
    description := "Utilities shared by all examples",
    publishArtifact := false // Don't publish this example in maven. Only the library.
  )

lazy val examplesCounter = Project(id = "examples-counter", base = file("examples/counter")).
  settings(
    commonSettings,
    name := "osm4scala-examples-counter",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.5.0"
    )
  ).dependsOn(core, commonUtilities)

lazy val examplesCounterParallel = Project(id = "examples-counter-parallel", base = file("examples/counter-parallel")).
  settings(
    commonSettings,
    name := "osm4scala-examples-counter-parallel",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel threads",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.5.0"
    )
  ).dependsOn(core, commonUtilities)

lazy val examplesCounterAkka = Project(id = "examples-counter-akka", base = file("examples/counter-akka")).
  settings(
    commonSettings,
    name := "osm4scala-examples-counter-akka",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel with AKKA",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.1",
      "com.github.scopt" %% "scopt" % "3.5.0"
    )
  ).dependsOn(core, commonUtilities)

lazy val examplesTagsExtraction = Project(id = "examples-tag-extraction", base = file("examples/tagsextraction")).
  settings(
    commonSettings,
    name := "osm4scala-examples-tags-extraction",
    description := "Extract all unique tags from the selected primitive type (Way, Node, Relation or All) using osm4scala",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.5.0"
    )
  ).dependsOn(core, commonUtilities)

lazy val examplesBlocksExtraction = Project(id = "examples-blocks-extraction", base = file("examples/blocksextraction")).
  settings(
    commonSettings,
    name := "osm4scala-examples-blocks-extraction",
    description := "Extract all blocks from the pbf into a folder using osm4scala.",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.5.0"
    )
  ).dependsOn(core, commonUtilities)

lazy val examplesPrimitivesExtraction = Project(id = "examples-primitives-extraction", base = file("examples/primitivesextraction")).
  settings(
    commonSettings,
    name := "osm4scala-examples-primitives-extraction",
    description := "Extract all primitives from the pbf into a folder using osm4scala.",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.5.0"
    )
  ).dependsOn(core, commonUtilities)

