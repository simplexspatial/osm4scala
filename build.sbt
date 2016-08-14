import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

coverageExcludedPackages := "org.openstreetmap.osmosis.osmbinary.*"

lazy val core = (project in file("core")).
  settings(
    PB.protobufSettings ++ Seq(
      organization := "com.acervera.pbf4scala",
      name := "pbf4scala",
      organizationHomepage := Some(url("http://www.acervera.com")),
      description := "Scala Pbf2 parser.",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.11.8",
      publishMavenStyle := true,
      crossScalaVersions := Seq("2.10.6", "2.11.8"),
      PB.grpc := false,
      scalaSource in PB.protobufConfig := sourceManaged.value / "main",
      libraryDependencies ++= List(
        "commons-io" % "commons-io" % "2.5" % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.7",
        "org.scalatest" %% "scalatest" % "2.2.6" % "test",
        "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
      )
    )
  )
