addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.34")

lazy val scalapbVersion = "0.10.2"
lazy val scalapbVersionPatch = "0.9.7"

/**
  * From version 0.9.7, ScalaPB drop Scala 2.11 comp.
  * I did the same at osm4scala 1.0.4, but at the moment, 90% of Cloud Spark providers
  * offers Spark services based on Scala 2.11.
  *
  * This is a patch that will allow to publish a Scala 2.11 version checking the ENV variable `PATCH_2_11`.
  * If `PATCH_2.11` is `true`, it will use scalapb 0.9.7. Otherwise, it will use the last version.
  *
  * @return Version to use.
  */
def getScalaPBVersion(): String = sys.env.getOrElse("PATCH_2.11", false) match {
  case true  => scalapbVersionPatch
  case false => scalapbVersion
}

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % getScalaPBVersion()
