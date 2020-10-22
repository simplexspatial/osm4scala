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

import com.acervera.osm4scala.model.NodeEntity
import com.acervera.osm4scala.utilities.DecompressUtils.{decompressChangeset, decompressCoord, decompressTimestamp, decompressUid, decompressUserSid, iteratorCheck}
import org.openstreetmap.osmosis.osmbinary.osmformat.{DenseInfo, DenseNodes, StringTable}

/**
  * Iterator over a DenseDataNode block.
  *
  * @param osmosisStringTable
  * @param osmosisDenseNode
  * @param latOffset
  * @param lonOffset
  * @param granularity
  */
case class DenseNodesIterator(osmosisStringTable: StringTable,
                         osmosisDenseNode: DenseNodes,
                         latOffset: Option[Long] = None,
                         lonOffset: Option[Long] = None,
                         granularity: Option[Int] = None,
                         dateGranularity: Option[Int] = None)
    extends Iterator[NodeEntity] {

  private val idIterator: Iterator[Long] = osmosisDenseNode.id.toIterator
  private val lonIterator: Iterator[Long]  = osmosisDenseNode.lon.toIterator
  private val latIterator: Iterator[Long]  = osmosisDenseNode.lat.toIterator
  private val tagsIterator: Iterator[Int] = osmosisDenseNode.keysVals.toIterator
  private val optionDenseInfo: Option[DenseInfo] = osmosisDenseNode.denseinfo
  private val versionIterator: Iterator[Int] = if(optionDenseInfo.isDefined) optionDenseInfo.get.version.toIterator else Iterator.empty
  private val timestampIterator: Iterator[Long] = if(optionDenseInfo.isDefined) optionDenseInfo.get.timestamp.toIterator else Iterator.empty
  private val changesetIterator: Iterator[Long]  = if(optionDenseInfo.isDefined) optionDenseInfo.get.changeset.toIterator else Iterator.empty
  private val uidIterator: Iterator[Int]  = if(optionDenseInfo.isDefined) optionDenseInfo.get.uid.toIterator else Iterator.empty
  private val userSidIterator: Iterator[Int]  = if(optionDenseInfo.isDefined) optionDenseInfo.get.userSid.toIterator else Iterator.empty
  private val visibleIterator: Iterator[Boolean]  = if(optionDenseInfo.isDefined) optionDenseInfo.get.visible.toIterator else Iterator.empty

  private val _latOffSet: Long = latOffset.getOrElse[Long](DenseNodesIterator.DEFAULT_LAT_OFFSET)
  private val _lonOffSet: Long = lonOffset.getOrElse[Long](DenseNodesIterator.DEFAULT_LON_OFFSET)
  private val _granularity: Int = granularity.getOrElse[Int](DenseNodesIterator.DEFAULT_GRANULARITY)
  private val _dateGranularity: Int = dateGranularity.getOrElse[Int](DenseNodesIterator.DEFAULT_DATE_GRANULARITY)

  private var lastNode: NodeEntity = NodeEntity(
    id = 0,
    latitude = 0,
    longitude = 0,
    tags = Map(),
    version = None,
    timestamp = None,
    changeset = None,
    uid = None,
    user_sid = None,
    visible = None
  )

  override def hasNext: Boolean = idIterator.hasNext

  override def next(): NodeEntity = {

    val id = idIterator.next() + lastNode.id
    val latitude =
      decompressCoord(_latOffSet, latIterator.next(), _granularity, lastNode.latitude)
    val longitude =
      decompressCoord(_lonOffSet, lonIterator.next(), _granularity, lastNode.longitude)
    val tags = tagsIterator
      .takeWhile(_ != 0L)
      .grouped(2)
      .map { tag =>
        osmosisStringTable.s(tag.head).toString("UTF-8") -> osmosisStringTable
          .s(tag.last)
          .toString("UTF-8")
      }
      .toMap

    // Create node
    lastNode = NodeEntity(
      id = id,
      latitude = latitude,
      longitude = longitude,
      tags = tags,
      version = iteratorCheck[Int](versionIterator),
      timestamp = decompressTimestamp(iteratorCheck[Long](timestampIterator), _dateGranularity, lastNode.timestamp),
      changeset = decompressChangeset(iteratorCheck[Long](changesetIterator), lastNode.changeset),
      uid = decompressUid(iteratorCheck[Int](uidIterator), lastNode.uid),
      user_sid = decompressUserSid(iteratorCheck[Int](userSidIterator), lastNode.user_sid),
      visible = iteratorCheck[Boolean](visibleIterator)
    )

    lastNode
  }

}

object DenseNodesIterator {
  private val DEFAULT_LAT_OFFSET = 0
  private val DEFAULT_LON_OFFSET = 0
  private val DEFAULT_GRANULARITY = 100
  private val DEFAULT_DATE_GRANULARITY = 1000
}