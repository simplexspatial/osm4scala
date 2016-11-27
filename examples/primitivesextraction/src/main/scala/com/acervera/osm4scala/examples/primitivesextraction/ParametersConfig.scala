package com.acervera.osm4scala.examples.primitivesextraction

import java.io.File

import com.acervera.osm4scala.model.OSMTypes

/**
  * Command line arguments parser.
  */
object ParametersConfig {

  // Parser
  case class Config(input: String = "", output: String = "")
  val parser = new scopt.OptionParser[Config]("extract-blocks") {
    opt[String]('i', "input").required().valueName("<file>").action((x, c) =>  c.copy(input  = x)).text("input is a required pbf2 format file")
    opt[String]('o', "output").required().valueName("<file>").action((x, c) =>  c.copy(output  = x)).text("output is a required folder path to store all blocks extracted.")
  }

}
