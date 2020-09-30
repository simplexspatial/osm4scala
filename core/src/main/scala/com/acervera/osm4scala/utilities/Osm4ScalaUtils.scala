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

package com.acervera.osm4scala.utilities

import java.io.{ByteArrayInputStream, DataInputStream}
import java.util.zip.Inflater

import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveGroup

object PrimitiveGroupType extends Enumeration {
  type PrimitiveGroupType = Value
  val Relations, Nodes, Ways, ChangeSets, DenseNodes, Unknown = Value
}

/**
  * Utilities to manage primitive groups.
  */
trait Osm4ScalaUtils {

  import PrimitiveGroupType._

  /**
    * Detect the type of group.
    *
    * @param group PrimitiveGroup object too be analysed.
    * @return Group detected ot Unknown if it is Unknown.
    */
  def detectType(group: PrimitiveGroup): PrimitiveGroupType = group match {
    case _ if group.relations.nonEmpty  => Relations
    case _ if group.nodes.nonEmpty      => Nodes
    case _ if group.ways.nonEmpty       => Ways
    case _ if group.changesets.nonEmpty => ChangeSets
    case _ if group.dense.isDefined     => DenseNodes
    case _                              => Unknown
  }

  /**
    * Generate an easy readable DataInputStream from a Blob.
    *
    * Because is a DataInputStream, it is really easily to access to Int and raw fields.
    * The source could be compressed, so we decompress it before read.
    * Follow the specification, every Blob can not be more of 32Mb, so we will not have memory problems.
    *
    * @param blob Blob from we extracted and uncompress the content.
    * @return A DataInputStream ready to read.
    */
  def dataInputStreamBlob(blob: Blob): DataInputStream = blob match {
    case _ if blob.raw.isDefined      => new DataInputStream(new ByteArrayInputStream(blob.raw.get.toByteArray))
    case _ if blob.zlibData.isDefined =>
      // Uncompress
      val inflater = new Inflater()
      val decompressedData = new Array[Byte](blob.rawSize.get)
      inflater.setInput(blob.zlibData.get.toByteArray)
      inflater.inflate(decompressedData)
      inflater.end()

      new DataInputStream(new ByteArrayInputStream(decompressedData))
    case _ => throw new Exception("Data not found even compressed.")
  }
}
