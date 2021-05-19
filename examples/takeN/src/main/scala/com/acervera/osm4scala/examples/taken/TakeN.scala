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

package com.acervera.osm4scala.examples.taken

import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.examples.taken.ParametersConfig._
import com.acervera.osm4scala.examples.utilities.Benchmarking
import com.acervera.osm4scala.utilities.Osm4ScalaUtils

import java.io._
import java.nio.file.{Files, Paths}

/**
  * Low level example about how take the N first blocks in a pbf file and write it in another.
  */
object TakeN extends App with Osm4ScalaUtils with Benchmarking {

  def fromPbf(inPbfPath: String, outPbfPath: String, blocksNumber: Int): Unit = {
    var pbfIS: InputStream = null
    var dsOS: DataOutputStream = null
    try {
      Files.createDirectories(Paths.get(outPbfPath).getParent)
      pbfIS = new FileInputStream(inPbfPath)
      dsOS = new DataOutputStream(new FileOutputStream(outPbfPath))
      BlobTupleIterator
        .fromPbf(pbfIS)
        .take(blocksNumber)
        .foreach {
          case (header, blob) =>
            dsOS.writeInt(header.serializedSize)
            header writeTo dsOS
            blob writeTo dsOS
        }
    } finally {
      if (pbfIS != null) pbfIS.close()
      if (dsOS != null) dsOS.close()
    }
  }

  // Logic that parse parameters.
  parser.parse(args, Config()) match {
    case Some(config) => {
      val (mms, _) = time { fromPbf(config.input, config.output, config.blocks) }
      println(f"Extracted ${config.blocks} blocks from ${config.input}%s file and stored in ${config.output} in ${mms * 1e-9}%,2.2f sec.")
    }
    case _ =>
  }

}
