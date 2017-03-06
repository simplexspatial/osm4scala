package com.acervera.osm4scala.examples.counterakka

import java.io.File

import com.acervera.osm4scala.model.OSMTypes

/**
  * Command line arguments parser.
  */
object ParametersConfig {

  // Translate string parameter type to enum type.
  implicit val osmTypesRead: scopt.Read[Option[OSMTypes.Value]] = scopt.Read.reads(txt=>{Some(OSMTypes withName txt)})

  // Parser
  case class Config(input: File = new File("."), osmType:Option[OSMTypes.Value] = None, actors: Int = Runtime.getRuntime().availableProcessors())
  val parser = new scopt.OptionParser[Config]("extract-primitives") {
    opt[File]('i', "input").required().valueName("<file>").action((x, c) => c.copy(input = x)).text("input is a required pbf2 format file")
    opt[Option[OSMTypes.Value]]('t', "type").optional().valueName("<type>").action((x, c) =>  c.copy(osmType  = x)).text("primitive type [Way, Node, Relation]")
    opt[Int]('a',"actors").optional().valueName("<number actors>").action((x,c) => c.copy(actors = x)).text("Number of actor to use. Default value is the number of cores")
  }

}
