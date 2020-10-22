/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017
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

/**
  * This Util object store the way to decompress osm entity fields. Based on wiki
  * here: https://wiki.openstreetmap.org/wiki/PBF_Format#File_format
  * We need to decompress coordinates, timestamp, changeset, uid and user_sid when
  * processing DenseNode and Dense Info
  */
object DecompressUtils {

  def iteratorCheck[A](iterator: Iterator[A]): Option[A] = {
    if(iterator.isEmpty || !iterator.hasNext) None else Option[A](iterator.next())
  }
  /**
    * Calculate coordinate applying offset, granularity and delta.
    *
    * @param offSet
    * @param delta
    * @param currentValue
    * @return
    */
  def decompressCoord(offSet: Long,
                      delta: Long,
                      granularity: Long,
                      currentValue: Double): Double = {
    (.000000001 * (offSet + (granularity * delta))) + currentValue
  }

  def decompressTimestamp(currentTimeStampOffSet: Option[Long],
                          dateGranularity: Int,
                          lastTimestamp: Option[Long]): Option[Long] = {
    currentTimeStampOffSet.map(
      offSet => offSet*dateGranularity + lastTimestamp.getOrElse(0L)
    )
  }

  def decompressChangeset(currentChangsetOffSet: Option[Long],
                          lastChangset: Option[Long]): Option[Long] = {
    currentChangsetOffSet.map(offSet => offSet + lastChangset.getOrElse(0L))
  }

  def decompressUid(currentUidOffSet: Option[Int],
                    lastUid: Option[Int]): Option[Int] = {
    currentUidOffSet.map(offSet => offSet + lastUid.getOrElse(0))
  }

  def decompressUserSid(currentUserUidOffSet: Option[Int],
                       lastUserSid: Option[Int]): Option[Int] = {
    currentUserUidOffSet.map(offSet => offSet + lastUserSid.getOrElse(0))
  }
}
