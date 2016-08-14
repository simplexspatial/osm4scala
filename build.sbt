import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*"

lazy val core = (project in file("core")).
  settings(
    PB.protobufSettings ++ Seq(
      organization := "com.acervera.osm4scala",
      name := "osm4scala-core",
      organizationHomepage := Some(url("http://www.acervera.com")),
      description := "Scala Open Street Map Pbf 2 parser.",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.11.8",
      publishMavenStyle := true,
      crossScalaVersions := Seq("2.10.6", "2.11.8"),
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
