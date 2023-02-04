/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ángel Cervera Claudio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import Dependencies._
import CommonSettings._

// Dependencies
lazy val sparkDefaultVersion = spark3Version

lazy val scala213 = "2.13.10"
lazy val scala212 = "2.12.17"
lazy val scala211 = "2.11.12"
lazy val scalaAllVersions = Seq(scala211, scala212, scala213)

def scalapbGen(ver: String) =
  (protocbridge.SandboxedJvmGenerator.forModule(
     "scala",
     protocbridge.Artifact(
       "com.thesamet.scalapb",
       s"compilerplugin_2.13",
       ver
     ),
     "scalapb.ScalaPbCodeGenerator$",
     Seq(
       protocbridge.Artifact(
         "com.thesamet.scalapb",
         "scalapb-runtime",
         ver,
         crossVersion = true
       ))
   ),
   Seq())

def getScalaPBVersion = Def.setting {
  println(">>>>>>> scalaVersion.value", scalaVersion.value)
  println(">>>>>>> CrossVersion.partialVersion", CrossVersion.partialVersion(scalaVersion.value))
  println(">>>>>>> CrossVersion.scalaApiVersion", CrossVersion.scalaApiVersion(scalaVersion.value))
  println(">>>>>>> CrossVersion.binaryScalaVersion", CrossVersion.binaryScalaVersion(scalaVersion.value))
  println(">>>>>>> CrossVersion.sbtApiVersion", CrossVersion.sbtApiVersion(scalaVersion.value))
  println(">>>>>>> CrossVersion.sbtApiVersion", CrossVersion.formatted(scalaVersion.value))
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 12 => "0.9.7"
    case x                      => "0.10.2"
  }
}

lazy val disablingPublishingSettings =
  Seq(publish / skip := true, publishArtifact := false)

lazy val enablingPublishingSettings = Seq(
  publishArtifact := true, // Enable publish
  publishMavenStyle := true,
  Test / publishArtifact := false
)

lazy val disablingCoverage = Seq(coverageEnabled := false)

lazy val coverageConfig =
  Seq(coverageMinimum := 80, coverageFailOnMinimum := true)

lazy val exampleSettings = disablingPublishingSettings ++ disablingCoverage

def generateSparkFatShadedModule(sparkVersion: String, sparkPrj: Project): Project =
  Project(
    id = s"osm4scala-spark${sparkVersion.head}-shaded",
    base = file(s"target/osm4scala-spark${sparkVersion.head}-shaded")
  ).disablePlugins(AssemblyPlugin)
    .settings(
      commonSettings,
      enablingPublishingSettings,
      disablingCoverage,
      name := s"osm4scala-spark${sparkVersion.head}-shaded",
      description := s"Spark ${sparkVersion.head} connector for OpenStreetMap Pbf parser as shaded fat jar.",
      Compile / packageBin := (sparkPrj / Compile / assembly).value
    )

def generateSparkModule(sparkVersion: String): Project = {

  val baseFolder = if (sparkDefaultVersion == sparkVersion) {
    s"spark"
  } else {
    s"target/spark${sparkVersion.head}"
  }

  def pathFromModule(relativePath: String): String =
    if (sparkDefaultVersion == sparkVersion) {
      relativePath
    } else {
      s"../../spark/$relativePath"
    }

  Project(id = s"spark${sparkVersion.head}", base = file(baseFolder))
    .enablePlugins(AssemblyPlugin)
    .settings(
      commonSettings,
      Compile / scalaSource := baseDirectory.value / pathFromModule("src/main/scala"),
      Compile / resourceDirectory := baseDirectory.value / pathFromModule("src/main/resources"),
      Test / scalaSource := baseDirectory.value / pathFromModule("src/test/scala"),
      Test / resourceDirectory := baseDirectory.value / pathFromModule("src/test/resources"),
      Test / parallelExecution := false,
      enablingPublishingSettings,
      coverageConfig,
      name := s"osm4scala-spark${sparkVersion.head}",
      description := s"Spark ${sparkVersion.head} connector for OpenStreetMap Pbf parser.",
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-sql" % sparkVersion % Provided
      ),
      assembly / assemblyOption := (assembly / assemblyOption).value.copy(
        includeScala = false,
        cacheUnzip = false,
        cacheOutput = false
      ),
      assembly / assemblyShadeRules := Seq(
        ShadeRule
          .rename("com.google.protobuf.**" -> "shadeproto.@1")
          .inAll
      )
    )
    .dependsOn(core)
}

lazy val spark2 = generateSparkModule(spark2Version)
  .settings(crossScalaVersions := Seq(scala211, scala212))

lazy val spark2FatShaded = generateSparkFatShadedModule(spark2Version, spark2)
  .settings(crossScalaVersions := Seq(scala211, scala212))

lazy val spark3 = generateSparkModule(spark3Version)
  .settings(crossScalaVersions := Seq(scala212, scala213))

lazy val spark3FatShaded = generateSparkFatShadedModule(spark3Version, spark3)
  .settings(crossScalaVersions := Seq(scala212, scala213))

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
    examplesBlocksExtraction,
    examplesTakeN,
    spark3,
    spark3FatShaded,
    exampleSparkUtilities,
    exampleSparkDocumentation
  )
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    name := "osm4scala-root",
    sonatypeProfileName := "com.acervera.osm4scala",
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
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val core = Project(id = "core", base = file("core"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    enablingPublishingSettings,
    coverageConfig,
    coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*",
    name := "osm4scala-core",
    description := "Scala OpenStreetMap Pbf 2 parser. Core",
    Compile / PB.targets := Seq(
      scalapbGen(getScalaPBVersion.value) -> (Compile / sourceManaged).value / "scalapb"
    )
//    Compile / PB.targets := Seq(
//      scalapb.gen(grpc = false) -> (Compile / sourceManaged).value
//    )
  )

// Examples

lazy val commonUtilities = Project(id = "examples-common-utilities", base = file("examples/common-utilities"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    exampleSettings,
    publish / skip := true,
    name := "osm4scala-examples-common-utilities",
    description := "Utilities shared by all examples",
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % scoptVersion)
  )
  .disablePlugins(AssemblyPlugin)

lazy val examplesCounter =
  Project(id = "examples-counter", base = file("examples/counter"))
    .disablePlugins(AssemblyPlugin)
    .settings(
      crossScalaVersions := scalaAllVersions,
      commonSettings,
      exampleSettings,
      name := "osm4scala-examples-counter",
      description := "Counter of primitives (Way, Node, Relation or All) using osm4scala"
    )
    .dependsOn(core, commonUtilities)

lazy val examplesCounterParallel = Project(id = "examples-counter-parallel", base = file("examples/counter-parallel"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-counter-parallel",
    description := "Counter of primitives (Way, Node, Relation or All) using osm4scala in parallel threads"
  )
  .dependsOn(core, commonUtilities)

lazy val examplesCounterAkka = Project(id = "examples-counter-akka", base = file("examples/counter-akka"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
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
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-tags-extraction",
    description := "Extract all unique tags from the selected primitive type (Way, Node, Relation or All) using osm4scala"
  )
  .dependsOn(core, commonUtilities)

lazy val examplesBlocksExtraction = Project(id = "examples-blocks-extraction", base = file("examples/blocksextraction"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-blocks-extraction",
    description := "Extract all blocks from the pbf into a folder using osm4scala."
  )
  .dependsOn(core, commonUtilities)

lazy val examplesBlocksWithIdExtraction =
  Project(id = "examples-blocks-with-id-extraction", base = file("examples/blockswithidextraction"))
    .disablePlugins(AssemblyPlugin)
    .settings(
      crossScalaVersions := scalaAllVersions,
      commonSettings,
      exampleSettings,
      name := "examples-blocks-with-id-extraction",
      description := "Extract blocks that contains the id from the pbf into a folder using osm4scala."
    )
    .dependsOn(core, commonUtilities)

lazy val examplesTakeN = Project(id = "examples-takeN", base = file("examples/takeN"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-takeN",
    description := "Generate a pbf file by taking the first N blocks."
  )
  .dependsOn(core, commonUtilities)

lazy val examplesPrimitivesExtraction =
  Project(id = "examples-primitives-extraction", base = file("examples/primitivesextraction"))
    .disablePlugins(AssemblyPlugin)
    .settings(
      crossScalaVersions := scalaAllVersions,
      commonSettings,
      exampleSettings,
      name := "osm4scala-examples-primitives-extraction",
      description := "Extract all primitives from the pbf into a folder using osm4scala."
    )
    .dependsOn(core, commonUtilities)

lazy val exampleSparkUtilities = Project(id = "examples-spark-utilities", base = file("examples/spark-utilities"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions := scalaAllVersions,
    commonSettings,
    exampleSettings,
    name := "osm4scala-examples-spark-utilities",
    description := "Example of different utilities using osm4scala and Spark.",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % spark3Version % Provided
    )
  )
  .dependsOn(spark3, commonUtilities)

lazy val exampleSparkDocumentation =
  Project(id = "examples-spark-documentation", base = file("examples/spark-documentation"))
    .disablePlugins(AssemblyPlugin)
    .settings(
      crossScalaVersions := scalaAllVersions,
      commonSettings,
      exampleSettings,
      name := "osm4scala-examples-spark-documentation",
      description := "Examples used in the documentation.",
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-sql" % spark3Version
      )
    )
    .dependsOn(spark3, commonUtilities)
