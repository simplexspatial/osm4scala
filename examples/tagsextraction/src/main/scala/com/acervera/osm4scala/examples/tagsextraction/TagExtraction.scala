package com.acervera.osm4scala.examples.tagsextraction

import java.io.{File, FileInputStream, InputStream, PrintWriter}

import com.acervera.osm4scala.EntityIterator._
import com.acervera.osm4scala.model.{OSMEntity, OSMTypes}
import ParametersConfig._
import com.acervera.osm4scala.examples.utilities.Benchmarking

import scala.annotation.tailrec

/**
  * Extract tags from a pbf file, optionally filtering by primitive type.
  */
object TagExtraction extends App with Benchmarking {

  @tailrec
  private def extractRecursive(iter: Iterator[OSMEntity], tags: Set[String]): Set[String] = iter match {
    case _ if iter.hasNext => extractRecursive(iter, tags ++ iter.next().tags.keySet) // This is the interesting code!
    case _ => tags
  }

  /**
    * Function that extract all distinct tags of the given primitives type in a osm pdf
    *
    * @param pbfIS InputStream with the osm pbf.
    * @param osmType Type of primitives to process.
    * @return Set of tags found.
    */
  def extract(pbfIS: InputStream, osmType: OSMTypes.Value) =
    extractRecursive(fromPbf(pbfIS).withFilter(_.osmModel == osmType), Set[String]())

  /**
    * Function that extract all distinct tags in a osm pdf
    *
    * @param pbfIS InputStream with the osm pbf.
    * @return Set of tags found.
    */
  def extract(pbfIS: InputStream) = {
    extractRecursive(fromPbf(pbfIS), Set[String]())
  }












  // Logic that parse parameters, open the file and call the osm4scala logic using "count"s
  // functions previously declared.
  parser.parse(args, Config()) match {
    case Some(config) if config.osmType == None =>
      var pbfIS:InputStream = null
      try {
        pbfIS = new FileInputStream(config.input)
        val result = time { extract(pbfIS) }
        println(f"Found [${result._2.size}%,d] different tags in ${config.input}. List stored in ${config.output}. Time to process: ${result._1 * 1e-9}%,2.2f sec.")
        storeSet(config.output, result._2)
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    case Some(config) =>
      var pbfIS:InputStream = null
      try {
        pbfIS = new FileInputStream(config.input)
        val result = time { extract(pbfIS, config.osmType.get) }
        println(f"Found [${result._2.size}%,d] different tags in primitives of type [${config.osmType.get}] in ${config.input}. List stored in ${config.output}. Time to process: ${result._1 * 1e-9}%,2.2f sec.")
        storeSet(config.output, result._2)
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    case None =>
  }

  private def storeSet(file: File, set: Set[String]) = {
    var writer:PrintWriter = null
    try {
      writer = new PrintWriter(file)
      set.foreach(writer.println)
    } finally {
      if (writer != null) writer.close()
    }
  }
}
