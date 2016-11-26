package com.acervera.osm4scala.examples.counter

import java.io.{FileInputStream, InputStream}

import com.acervera.osm4scala.EntityIterator._
import com.acervera.osm4scala.model.{OSMEntity, OSMTypes}
import ParametersConfig._
import com.acervera.osm4scala.examples.utilities.Benchmarking

/**
  * Example that count the number of primitives in the pbf file.
  *
  */
object Counter extends App with Benchmarking {

  /**
    * Function that count the number of primitives of a type in a osm pdf
    *
    * @param pbfIS InputStream with the osm pbf.
    * @param osmType Type of primitives to count.
    * @return Number of primitives found.
    */
  def count(pbfIS: InputStream, osmType: OSMTypes.Value): Long =
    count( fromPbf(pbfIS).filter(_.osmModel == osmType) ) // .size is not valid because return a int, so too small type for the full planet.
    // fromPbf(pbfIS).count(_.osmModel == osmType)  // This is the interesting code!

  /**
    * Function that count the number of primitives in a osm pdf
    *
    * @param pbfIS InputStream with the osm pbf.
    * @return Number of primitives found.
    */
  def count(pbfIS: InputStream): Long = count(fromPbf(pbfIS)) // .size is not valid because return a int, so too small type for the full planet.
    // fromPbf(pbfIS).size // This is the interesting code!

  /**
    * Count the number of items in the iterator.
    *
    * @param entityIterator Entities iterator
    * @return Count of elements.
    */
  def count(entityIterator: Iterator[OSMEntity]) =
    entityIterator.foldLeft(0L)( (acc, _) => acc+1L) // .size is not valid because return a int, so too small type for the full planet.







  // Logic that parse parameters, open the file and call the osm4scala logic using "count"s
  // functions previously declared.
  parser.parse(args, Config()) match {
    case Some(config) if config.osmType == None =>
      var pbfIS:InputStream = null
      try {
        pbfIS = new FileInputStream(config.input)
        val result = time { count(pbfIS) }
        println(f"Found [${result._2}%,d] primitives in ${config.input} in ${result._1 * 1e-9}%,2.2f sec.")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    case Some(config) =>
      var pbfIS:InputStream = null
      try {
        pbfIS = new FileInputStream(config.input)
        val result = time { count(pbfIS, config.osmType.get) }
        println(f"Found [${result._2}%,d] primitives of type [${config.osmType.get}] in ${config.input} in ${result._1 * 1e-9}%,2.2f sec.")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    case None =>
  }

}
