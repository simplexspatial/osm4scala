package com.acervera.osm4scala.examples.counterparallel

import java.io.{FileInputStream, InputStream}
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.ExecutionContext.Implicits.global
import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.EntityIterator._
import ParametersConfig._
import com.acervera.osm4scala.model.OSMTypes
import com.acervera.osm4scala.examples.utilities.Benchmarking
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Example that count the number of primitives in the pbf file.
  *
  */
object CounterParallel extends App with Benchmarking {

  def count(blob: Blob, osmType: OSMTypes.Value) =
    fromBlob(blob).count(_.osmModel == osmType)

  def count(blob: Blob) =
    fromBlob(blob).length

  val counter = new AtomicLong()

  def count(pbfIS: InputStream, osmType: OSMTypes.Value): Long = {
    val result = Future.traverse(BlobTupleIterator.fromPbf(pbfIS))(tuple => Future {
      counter.addAndGet( count(tuple._2, osmType) )
    })

    Await.result(result, Duration.Inf)

    counter.longValue()
  }

  def count(pbfIS: InputStream): Long = {
    val result = Future.traverse(BlobTupleIterator.fromPbf(pbfIS))(tuple => Future {
      counter.addAndGet( count(tuple._2) )
      None
    })

    Await.result(result, Duration.Inf)

    counter.longValue()
  }



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
