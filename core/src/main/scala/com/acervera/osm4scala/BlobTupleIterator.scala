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

package com.acervera.osm4scala

import java.io._

import org.openstreetmap.osmosis.osmbinary.fileformat.{Blob, BlobHeader}

object BlobTupleIterator {

  /**
    * Create a new BlobTupleIterator iterator from a IntputStream pbf format.
    *
    * @param pbfInputStream Opened InputStream that contains the pbf
    * @return
    */
  def fromPbf(pbfInputStream: InputStream): BlobTupleIterator = new BlobTupleIterator(new DefaultInputStreamSentinel(pbfInputStream))

  /**
    * Create a new BlobTupleIterator iterator from a IntputStream pbf format.
    *
    * @param pbfInputStream Opened InputStream that contains the pbf
    * @return
    */
  def fromPbf(pbfInputStream: InputStreamSentinel): BlobTupleIterator = new BlobTupleIterator(pbfInputStream)

}

/**
  * Iterator over a OSM file in pbf format.
  * Each item is a tuple of BlobHeader and Blob
  *
  * @param pbfInputStream Input stream that will be used to read the fileblock
  * @author angelcervera
  */
class BlobTupleIterator(pbfInputStream: InputStreamSentinel) extends Iterator[(BlobHeader, Blob)] {

  // Read the input stream using DataInputStream to access easily to Int and raw fields.
  val pbfStream = new DataInputStream(pbfInputStream)

  // Store the next block length. None if there are not more to read.
  var nextBlockLength: Option[Int] = None

  // Read the length of the next block
  readNextBlockLength()

  override def hasNext: Boolean = pbfInputStream.continueReading() && nextBlockLength.isDefined

  override def next(): (BlobHeader, Blob) = {

    // Reading header.
    val bufferBlobHeader = new Array[Byte](nextBlockLength.get)
    pbfStream.readFully(bufferBlobHeader)

    // Parsing pbf header.
    val blobHeader = BlobHeader parseFrom bufferBlobHeader

    // Read the next block
    val bufferBlob = new Array[Byte](blobHeader.datasize)
    pbfStream.readFully(bufferBlob)
    val blob = Blob parseFrom bufferBlob

    // Move to the next pair.
    readNextBlockLength()

    (blobHeader, blob)

  }

  /**
    * Read the next osm pbf block
    */
  private def readNextBlockLength(): Unit =
    try {
      nextBlockLength = Some(pbfStream.readInt)
    } catch {
      case _: EOFException => nextBlockLength = None
    }

}
