/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ángel Cervera Claudio
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

package com.acervera.osm4scala.examples.blockswithidextraction

import com.acervera.osm4scala.examples.blockswithidextraction.ParametersConfig.{Config, parser}
import com.acervera.osm4scala.examples.utilities.Benchmarking
import com.acervera.osm4scala.utilities.Osm4ScalaUtils
import com.acervera.osm4scala.{BlobTupleIterator, EntityIterator}
import org.openstreetmap.osmosis.osmbinary.fileformat.{Blob, BlobHeader}
import scalapb.GeneratedMessage

import java.io.{FileInputStream, FileOutputStream, InputStream}
import java.nio.file.{Files, Paths}

/**
  * Low level example about how to uncompress and extract all data blocks to a folder.
  * In this case, I'm going to extract
  *
  * In this example, I am writing all blocks in a folders. Rememeber that this block is a Blob, so contains
  * the string table and the possble compressed data.
  */
object BlocksWithIdExtraction extends App with Osm4ScalaUtils with Benchmarking {

  def fromPbf(pbfFilePath: String, extractRootFolder: String, id: Long): Long = {
    var pbfIS: InputStream = null
    try {
      pbfIS = new FileInputStream(pbfFilePath)
      Files.createDirectories(Paths.get(extractRootFolder))
      BlobTupleIterator
        .fromPbf(pbfIS)
        .foldLeft(0L) {
          case (counter, (header, blob)) =>
            if(containsId(header, blob, id)) {
              write(s"$extractRootFolder/${counter}_${header.`type`}.blob", blob)
              write(s"$extractRootFolder/${counter}_${header.`type`}.header", header)
            }
            counter + 1
        }
    } finally {
      if (pbfIS != null) pbfIS.close()
    }
  }

  def containsId(header: BlobHeader, blob: Blob, id: Long): Boolean =
    header.`type` match {
      case "OSMData" => EntityIterator.fromBlob(blob).exists(p => p.id == id)
      case _ => false
    }

  private def write(outPath: String, message: GeneratedMessage) {
    val output = new FileOutputStream(outPath)
    message writeTo output
    output.close
  }

  // Logic that parse parameters.
  parser.parse(args, Config()) match {
    case Some(config) =>
      val result = time { fromPbf(config.source, config.output, config.id) }
      println(f"Extracted ${config.source}%s file in ${config.output} in ${result._1 * 1e-9}%,2.2f sec.")
      println(s"Resume: ${result._2} Blob blocks")
    case _ =>
  }

}
