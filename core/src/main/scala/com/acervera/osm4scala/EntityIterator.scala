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

import java.io.InputStream

import com.acervera.osm4scala.model.OSMEntity
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob

/**
  * Iterable process all entities.
  */
trait EntityIterator extends Iterator[OSMEntity]

/**
  * Factory to create EntityIterator objects from different sources.
  */
object EntityIterator {

  /**
    * Create an iterator to iterate over all entities in the InputStream.
    *
    * @param pbfInputStream InputStream object to process
    * @return Iterator
    */
  def fromPbf(pbfInputStream: InputStream): EntityIterator = new FromPbfFileEntitiesIterator(pbfInputStream)

  /**
    * Create an iterator to iterate over all entities in th Blob.
    *
    * @param blob Blob object to process
    * @return Iterator
    */
  def fromBlob(blob: Blob): EntityIterator = new FromBlobEntitiesIterator(blob)

}
