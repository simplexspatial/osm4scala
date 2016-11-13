package com.acervera.osm4scala.examples.counter

import java.io.File

import com.acervera.osm4scala.model.OSMTypes

/**
  * Created by angelcervera on 13/11/16.
  */
object ParametersConfig {

  // Command line arguments parser.
  implicit val osmTypesRead: scopt.Read[Option[OSMTypes.Value]] = scopt.Read.reads(txt=>{Some(OSMTypes withName txt)})
  case class Config(input: File = new File("."), osmType:Option[OSMTypes.Value] = None)
  val parser = new scopt.OptionParser[Config]("extract-primitives") {
    opt[File]('i', "input").required().valueName("<file>").action((x, c) =>  c.copy(input  = x)).text("input is a required pbf2 format file")
    opt[Option[OSMTypes.Value]]('t', "type").optional().valueName("<type>").action((x, c) =>  c.copy(osmType  = x)).text("primitive type [Way, Node, Relation]")
  }



}
