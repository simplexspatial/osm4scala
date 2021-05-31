val scalapbVersion = "0.10.2"
val scalapbVersionPatch = "0.9.7"

/**
  * From version 0.9.7, ScalaPB drop Scala 2.11 comp.
  * I did the same at osm4scala 1.0.4, but at the moment, 90% of Cloud Spark providers
  * offers Spark services based on Scala 2.11.
  *
  * This is a patch that will allow to publish a Scala 2.11 version checking the ENV variable `PATCH_2_11`.
  * If `PATCH_211` is `true`, it will use scalapb 0.9.7. Otherwise, it will use the last version.
  *
  * @return Version to use.
  */
def getScalaPBVersion(): String = sys.env.getOrElse("PATCH_211", "false").toBoolean match {
  case true  => scalapbVersionPatch
  case false => scalapbVersion
}

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.34")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % getScalaPBVersion()

logLevel := Level.Warn

// Helpers for dev time.
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.20")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")
