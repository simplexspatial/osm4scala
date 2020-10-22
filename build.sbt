import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

// Dependencies
lazy val scalatestVersion = "3.2.0"
lazy val scalacheckVersion = "1.14.3"
lazy val commonIOVersion = "2.5"
lazy val logbackVersion = "1.1.7"
lazy val scoptVersion = "3.7.1"
lazy val akkaVersion = "2.5.31"
lazy val spark2Version = "2.4.7"
lazy val sparkDefaultVersion = spark2Version

// Releases versions
lazy val scala211 = "2.11.12"

lazy val commonSettings = Seq(
  scalaVersion := scala211,
  organization := "com.acervera.osm4scala",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage in ThisBuild := Some(
    url(s"https://github.com/simplexspatial/osm4scala")
  ),
  scmInfo in ThisBuild := Some(
    ScmInfo(
      url("https://github.com/simplexspatial/osm4scala"),
      "scm:git:git://github.com/simplexspatial/osm4scala.git",
      "scm:git:ssh://github.com:simplexspatial/osm4scala.git"
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
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
    "commons-io" % "commons-io" % commonIOVersion % Test
  ),
  test in assembly := {},
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "utf8",
    "-deprecation",
    "-unchecked",
    "-Xlint"
  ),
  javacOptions ++= Seq(
    "-Xlint:all",
    "-source",
    "1.8",
    "-target",
    "1.8",
    "-parameters"
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
  bintrayVcsUrl := Some("https://github.com/simplexspatial/osm4scala.git")
)

lazy val exampleSettings = disablingPublishingSettings


def generateSparkFatShadedModule(sparkVersion: String, sparkPrj: Project): Project =
    Project(
      id = s"osm4scala-spark${sparkVersion.head}-shaded",
      base = file(s"target/osm4scala-spark${sparkVersion.head}-shaded")
    )
      .disablePlugins(AssemblyPlugin)
      .settings(
        commonSettings,
        enablingPublishingSettings,
        name := s"osm4scala-spark${sparkVersion.head}-shaded",
        description := "Spark 2 connector for OpenStreetMap Pbf parser as shaded fat jar.",
        bintrayPackage := s"osm4scala-spark${sparkVersion.head}-shaded",
        packageBin in Compile := (assembly in(sparkPrj, Compile)).value
      )

def generateSparkModule(sparkVersion: String): Project = {

  val baseFolder = if(sparkDefaultVersion == sparkVersion) {
    s"spark"
  } else {
    s"target/spark${sparkVersion.head}"
  }

  Project(id = s"spark${sparkVersion.head}", base = file(baseFolder))
  .enablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    scalaSource in Compile := baseDirectory.value / ".." / "spark" / "src" / "main" / "scala",
    scalaSource in Test := baseDirectory.value / ".." / "spark" / "src" / "test" / "scala",
    enablingPublishingSettings,
    name := s"osm4scala-spark${sparkVersion.head}",
    description := "Spark 2 connector for OpenStreetMap Pbf parser.",
    bintrayPackage := s"osm4scala-spark${sparkVersion.head}",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion % Provided
    ),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(
      includeScala = false,
      cacheUnzip = false,
      cacheOutput = false
    ),
    assemblyShadeRules in assembly := Seq(
      ShadeRule
        .rename("com.google.protobuf.**" -> "shadeproto.@1")
        .inAll
    )
  )
  .dependsOn(core)
}

lazy val spark2 = generateSparkModule(spark2Version)
lazy val spark2FatShaded = generateSparkFatShadedModule(spark2Version, spark2)

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
lazy val root = (project in file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    core,
    spark2,
    spark2FatShaded,
    commonUtilities,
    examplesCounter,
    examplesCounterParallel,
    examplesCounterAkka,
    examplesTagsExtraction,
    examplesPrimitivesExtraction,
    exampleSparkUtilities
  )
  .settings(
    name := "osm4scala-root",
    publish / skip := true,
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

lazy val core = Project(id = "core", base = file("core"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    enablingPublishingSettings,
    name := "osm4scala-core",
    description := "Scala OpenStreetMap Pbf 2 parser. Core",
    bintrayPackage := "osm4scala-core",
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = false) -> (sourceManaged in Compile).value
    )
  )




// Examples

lazy val commonUtilities = Project(id = "examples-common-utilities", base = file("examples/common-utilities"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    exampleSettings,
    skip in publish := true,
    name := "osm4scala-examples-common-utilities",
    description := "Utilities shared by all examples",
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
  )
  .disablePlugins(AssemblyPlugin)

lazy val examplesCounter =
  Project(id = "examples-counter", base = file("examples/counter"))
    .disablePlugins(AssemblyPlugin)
    .settings(
      commonSettings,
      exampleSettings,
      name := "osm4scala-examples-counter",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala"
    )
    .dependsOn(core, commonUtilities)

lazy val examplesCounterParallel = Project(id = "examples-counter-parallel", base = file("examples/counter-parallel"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-counter-parallel",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel threads"
  )
  .dependsOn(core, commonUtilities)

lazy val examplesCounterAkka = Project(id = "examples-counter-akka", base = file("examples/counter-akka"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-counter-akka",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel with AKKA",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion
    )
  )
  .dependsOn(core, commonUtilities)

lazy val examplesTagsExtraction = Project(id = "examples-tag-extraction", base = file("examples/tagsextraction"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-tags-extraction",
    description := "Extract all unique tags from the selected primitive type (Way, Node, Relation or All) using osm4scala"
  )
  .dependsOn(core, commonUtilities)

lazy val examplesBlocksExtraction = Project(id = "examples-blocks-extraction", base = file("examples/blocksextraction"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-blocks-extraction",
    description := "Extract all blocks from the pbf into a folder using osm4scala."
  )
  .dependsOn(core, commonUtilities)

lazy val examplesPrimitivesExtraction =
  Project(id = "examples-primitives-extraction", base = file("examples/primitivesextraction"))
    .disablePlugins(AssemblyPlugin)
    .settings(
      commonSettings,
      exampleSettings,
      name := "osm4scala-examples-primitives-extraction",
      description := "Extract all primitives from the pbf into a folder using osm4scala."
    )
    .dependsOn(core, commonUtilities)

lazy val exampleSparkUtilities = Project(id = "examples-spark-utilities", base = file("examples/spark-utilities"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-spark-utilities",
    description := "Example of different utilities using osm4scala and Spark.",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % spark2Version % Provided
    )
  )
  .dependsOn(spark2, commonUtilities)
