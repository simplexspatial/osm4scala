package com.acervera.osm4scala.examples.counter

import java.io.{File, FileInputStream, InputStream}

import com.acervera.osm4scala.PbfFileIterator
import com.acervera.osm4scala.model.OSMTypes

/**
  * Created by angelcervera on 16/10/16.
  */
object Counter extends App {

  // Command line arguments parser.
  implicit val osmTypesRead: scopt.Read[Option[OSMTypes.Value]] = scopt.Read.reads(txt=>{Some(OSMTypes withName txt)})
  case class Config(input: File = new File("."), osmType:Option[OSMTypes.Value] = None)
  val parser = new scopt.OptionParser[Config]("extract-primitives") {
    opt[File]('i', "input").required().valueName("<file>").action((x, c) =>  c.copy(input  = x)).text("input is a required pbf2 format file")
    opt[Option[OSMTypes.Value]]('t', "type").optional().valueName("<type>").action((x, c) =>  c.copy(osmType  = x)).text("primitive type [Way, Node, Relation]")
  }

  def time[R](block: => R): (Long,R) = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    ((t1 - t0), result)
  }

  parser.parse(args, Config()) match {
    case Some(config) if config.osmType == None =>
      val pbfIS:InputStream = null
      try {
        val pbfIS = new FileInputStream(config.input)
        val result = time { count(pbfIS) }
        println(f"Found [${result._2}%,d] primitives in ${config.input} in ${result._1 * 1e-9}%,2.2f sec.")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    case Some(config) =>
      val pbfIS:InputStream = null
      try {
        val pbfIS = new FileInputStream(config.input)
        val result = time { count(pbfIS, config.osmType.get) }
        println(f"Found [${result._2}%,d] primitives of type [${config.osmType.get}] in ${config.input} in ${result._1 * 1e-9}%,2.2f sec.")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    case None =>
  }

  /**
    * Function that count the number of primitives of a type in a osm pdf
    *
    * @param pbfIS InputStream with the osm pbf.
    * @param osmType Type of primitives to count.
    * @return Number of primitives found.
    */
  def count(pbfIS: InputStream, osmType: OSMTypes.Value): Long =
    PbfFileIterator(pbfIS).count(_.osmModel == osmType)  // This is the interesting code!

  /**
    * Function that count the number of primitives in a osm pdf
    *
    * @param pbfIS InputStream with the osm pbf.
    * @return Number of primitives found.
    */
  def count(pbfIS: InputStream): Long =
    PbfFileIterator(pbfIS).size  // This is the interesting code!

}
