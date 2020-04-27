import ReleaseTransformations._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.{releaseCrossBuild, _}

publishArtifact := false // Avoid publish default artifact

// Releases versions
lazy val scala213 = "2.13.2"
lazy val scala212 = "2.12.11"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala213, scala212, scala211)

// Dependencies
lazy val scalatestVersion = "3.0.8"
lazy val scalacheckVersion = "1.14.3"
lazy val commonIOVersion = "2.5"
lazy val logbackVersion = "1.1.7"
lazy val scoptVersion = "3.7.1"

// crossScalaVersions must be set to Nil on the aggregating project
crossScalaVersions := Nil

// Bintray BUG workaround: https://github.com/softprops/bintray-sbt/issues/93
bintrayRelease := false
bintrayEnsureBintrayPackageExists := false
bintrayEnsureLicenses := false

lazy val commonSettings = Seq(
  organization := "com.acervera.osm4scala",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  publishArtifact := false,
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
      </developers>,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",
    "commons-io" % "commons-io" % commonIOVersion % "test"
  )
)

lazy val core = Project(id = "core", base = file("core")).settings(
  commonSettings,
  crossScalaVersions := supportedScalaVersions,
  name := "osm4scala-core",
  description := "Scala Open Street Map Pbf 2 parser. Core",
  coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*",
  PB.targets in Compile := Seq(
    scalapb.gen(grpc = false) -> (sourceManaged in Compile).value
  ),
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion
  ),
  releaseCrossBuild := false,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("+test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishArtifacts"), //     releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  // Publish
  publishArtifact := true, // Enable publish
  publishMavenStyle := true,
  publishArtifact in Test := false, // No publish test stuff
  // Bintray
  bintrayRepository := "maven",
  bintrayPackage := "osm4scala",
  bintrayReleaseOnPublish := false,
  bintrayRelease := false
)

// Examples

lazy val commonUtilities = Project(
  id = "examples-common-utilities",
  base = file("examples/common-utilities")
).settings(
  commonSettings,
  skip in publish := true,
  crossScalaVersions := supportedScalaVersions,
  name := "osm4scala-examples-common-utilities",
  description := "Utilities shared by all examples",
)

lazy val examplesCounter =
  Project(id = "examples-counter", base = file("examples/counter"))
    .settings(
      commonSettings,
      skip in publish := true,
      crossScalaVersions := supportedScalaVersions,
      name := "osm4scala-examples-counter",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala",
      libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
    )
    .dependsOn(core, commonUtilities)

lazy val examplesCounterParallel = Project(
  id = "examples-counter-parallel",
  base = file("examples/counter-parallel")
).settings(
    commonSettings,
    skip in publish := true,
    crossScalaVersions := supportedScalaVersions,
    name := "osm4scala-examples-counter-parallel",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel threads",
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
  )
  .dependsOn(core, commonUtilities)

lazy val examplesCounterAkka =
  Project(id = "examples-counter-akka", base = file("examples/counter-akka"))
    .settings(
      commonSettings,
      skip in publish := true,
      crossScalaVersions := supportedScalaVersions,
      name := "osm4scala-examples-counter-akka",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel with AKKA",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.5.31",
        "com.github.scopt" %% "scopt" % scoptVersion
      )
    )
    .dependsOn(core, commonUtilities)

lazy val examplesTagsExtraction = Project(
  id = "examples-tag-extraction",
  base = file("examples/tagsextraction")
).settings(
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    name := "osm4scala-examples-tags-extraction",
    description := "Extract all unique tags from the selected primitive type (Way, Node, Relation or All) using osm4scala",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
  )
  .dependsOn(core, commonUtilities)

lazy val examplesBlocksExtraction = Project(
  id = "examples-blocks-extraction",
  base = file("examples/blocksextraction")
).settings(
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    name := "osm4scala-examples-blocks-extraction",
    description := "Extract all blocks from the pbf into a folder using osm4scala.",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
  )
  .dependsOn(core, commonUtilities)

lazy val examplesPrimitivesExtraction = Project(
  id = "examples-primitives-extraction",
  base = file("examples/primitivesextraction")
).settings(
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    name := "osm4scala-examples-primitives-extraction",
    description := "Extract all primitives from the pbf into a folder using osm4scala.",
    publishArtifact := false, // Don't publish this example in maven. Only the library.
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
  )
  .dependsOn(core, commonUtilities)
