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

package com.acervera.osm4scala.examples.blocksextraction

import java.io._
import java.nio.file.{Files, Paths}

import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.utilities.Osm4ScalaUtils
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import ParametersConfig._
import com.acervera.osm4scala.examples.utilities.Benchmarking

/**
  * Low level example about how to uncompress and extract all data blocks to a folder.
  * Usually, it is not necessary to do it, but is good, for example, to extract fragments from the pbf that represent
  * data blocks and then use them to test the data block reader.
  *
  * In this example, I am writing all blocks in a folders. Rememeber that this block is a Blob, so contains
  * the string table and the possble compressed data.
  */
object BlocksExtraction extends App with Osm4ScalaUtils with Benchmarking {

  def fromPbf(pbfFilePath: String, extractRootFolder: String): Long = {
    var pbfIS: InputStream = null
    try {
      pbfIS = new FileInputStream(pbfFilePath)
      Files.createDirectories(Paths.get(extractRootFolder))
      var x = BlobTupleIterator.fromPbf(pbfIS).foldLeft(0L)((counter,x) => x match {
        case _ if (x._1.`type` == "OSMData") => {
          writeBlob(x._2, extractRootFolder, counter)
        }
        case _ =>  counter
      })
      x
    } finally {
      if (pbfIS != null) pbfIS.close()
    }
  }



  def writeBlob(blob: Blob, outputFolderPath: String, counter: Long): Long = {
    val output = new FileOutputStream(s"$outputFolderPath/${counter}.blob")
    blob writeTo output
    output.close

    counter+1
  }

  // Logic that parse parameters.
  parser.parse(args, Config()) match {
    case Some(config) => {
      val result = time { fromPbf(config.input, config.output) }
      println(f"Extracted ${config.input}%s file in ${config.input} in ${result._1 * 1e-9}%,2.2f sec.")
      println(s"Resume: ${result._2} Blob blocks")
    }
    case _ =>
  }

}
