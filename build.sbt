import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings
PB.grpc := false

lazy val root = (project in file(".")).
  settings(
    organization := "com.acervera.pbf4scala",
    name := "pbf4scala",
    organizationHomepage := Some(url("http://www.acervera.com")),
    description := "Scala Pbf2 parser.",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    publishMavenStyle := true,
    libraryDependencies ++= List(
      "commons-codec" % "commons-codec" % "1.10" % "test",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
    )
  )