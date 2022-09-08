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

import com.acervera.osm4scala.model.{Info, NodeEntity}
import com.acervera.osm4scala.utilities.StringTableUtils._
import org.openstreetmap.osmosis.osmbinary.osmformat.{DenseInfo, DenseNodes, StringTable}

import java.time.Instant

object DenseNodesIterator {

  def apply(osmosisStringTable: StringTable, osmosisDenseNode: DenseNodes): DenseNodesIterator =
    new DenseNodesIterator(osmosisStringTable, osmosisDenseNode)

}

/**
  * Iterator over a DenseDataNode block.
  * By default, lanOffset, longOffset and graularity is 0, 0 and 100 because I did not found pbf files with other values.
  *
  * @param osmosisStringTable
  * @param osmosisDenseNode
  * @param latOffset
  * @param lonOffset
  * @param granularity
  */
class DenseNodesIterator(osmosisStringTable: StringTable,
                         osmosisDenseNode: DenseNodes,
                         latOffset: Long = 0,
                         lonOffset: Long = 0,
                         granularity: Int = 100)
    extends Iterator[NodeEntity] {

  if (osmosisDenseNode.denseinfo.isDefined && osmosisDenseNode.denseinfo.get.visible.exists(b => !b)) {
    throw new Exception("Only visible nodes are implemented.")
  }

  private val idIterator = osmosisDenseNode.id.iterator
  private val lonIterator = osmosisDenseNode.lon.iterator
  private val latIterator = osmosisDenseNode.lat.iterator
  private val tagsIterator = osmosisDenseNode.keysVals.iterator
  private val infoIterator = InfoIterator(osmosisDenseNode.denseinfo)

  // Delta references
  var lastId = 0L
  var lastLatitude = 0.0
  var lastLongitude = 0.0

  override def hasNext: Boolean = idIterator.hasNext

  override def next(): NodeEntity = {

    // Calculate new values base in deltas and update deltas
    lastId = idIterator.next() + lastId
    lastLatitude = decompressCoord(latOffset, latIterator.next(), lastLatitude)
    lastLongitude = decompressCoord(lonOffset, lonIterator.next(), lastLongitude)

    // Create node
    NodeEntity(
      lastId,
      lastLatitude,
      lastLongitude,
      osmosisStringTable.extractTags(tagsIterator.takeWhile(_ != 0L)),
      infoIterator.next()
    )

  }

  /**
    * Calculate coordinate applying offset, granularity and delta.
    *
    * @param offSet
    * @param delta
    * @param currentValue
    * @return
    */
  def decompressCoord(offSet: Long, delta: Long, currentValue: Double): Double = {
    (.000000001 * (offSet + (granularity * delta))) + currentValue
  }

  // Decode DenseInfo

  trait InfoIterator extends Iterator[Option[Info]]

  class InfoIteratorDeltas(denseInfo: DenseInfo) extends InfoIterator {
    val versionIterator = denseInfo.version.iterator
    val timestampIterator = denseInfo.timestamp.iterator
    val changesetIterator = denseInfo.changeset.iterator
    val uidIterator = denseInfo.uid.iterator
    val userNameIterator = denseInfo.userSid.iterator
    val visibleIterator = denseInfo.visible.iterator

    var lastTimestamp = 0L
    var lastChangeset = 0L
    var lastUserId = 0
    var lastUserName = 0

    override def hasNext: Boolean =
      versionIterator.hasNext ||
        timestampIterator.hasNext ||
        changesetIterator.hasNext ||
        uidIterator.hasNext ||
        userNameIterator.hasNext ||
        visibleIterator.hasNext

    override def next(): Option[Info] = {

      val newTimestamp = if (timestampIterator.hasNext) {
        lastTimestamp = lastTimestamp + timestampIterator.next()
        Some(Instant.ofEpochSecond(lastTimestamp))
      } else {
        None
      }

      val newChangeset = if (changesetIterator.hasNext) {
        lastChangeset = lastChangeset + changesetIterator.next()
        Some(lastChangeset)
      } else {
        None
      }

      val newUserId = if (uidIterator.hasNext) {
        lastUserId = lastUserId + uidIterator.next()
        Some(lastUserId)
      } else {
        None
      }

      val newUserName = if (userNameIterator.hasNext) {
        lastUserName = lastUserName + userNameIterator.next()
        Some(osmosisStringTable.getString(lastUserName))
      } else {
        None
      }

      val info = Info(
        version = if (versionIterator.hasNext) Some(versionIterator.next()) else None,
        timestamp = newTimestamp,
        changeset = newChangeset,
        userId = newUserId,
        userName = newUserName,
        visible = if (visibleIterator.hasNext) Some(visibleIterator.next()) else None
      )

      Some(info)
    }
  }

  object InfoIterator {
    def apply(denseInfo: Option[DenseInfo]): InfoIterator = denseInfo match {
      case None       => InfoIteratorNone
      case Some(info) => new InfoIteratorDeltas(info)
    }
  }

  object InfoIteratorNone extends InfoIterator {
    override def next(): Option[Info] = None
    override def hasNext: Boolean = true
  }

}
