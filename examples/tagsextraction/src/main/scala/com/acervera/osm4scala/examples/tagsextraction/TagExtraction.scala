/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ángel Cervera Claudio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
