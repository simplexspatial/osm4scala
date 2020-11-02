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

package com.acervera.osm4scala.model

import com.acervera.osm4scala.utilities.DecompressUtils
import org.openstreetmap.osmosis.osmbinary.osmformat.{Info, StringTable, Way}

/**
  * Entity that represent a OSM way as https://wiki.openstreetmap.org/wiki/Elements#Way and https://wiki.openstreetmap.org/wiki/Way describe
  */
case class WayEntity(id: Long,
                     nodes: Seq[Long],
                     tags: Map[String, String],
                     version: Option[Int],
                     timestamp: Option[Long],
                     changeset: Option[Long],
                     uid: Option[Int],
                     user: Option[String],
                     visible: Option[Boolean]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Way

  def apply(id: Long,
            nodes: Seq[Long],
            tags: Map[String, String]): WayEntity = {
    WayEntity(id, nodes, tags,
      None, None, None, None, None, None)
  }

  override def toString: String = {
    s"Way id: ${id}, " +
      s"nodes: ${nodes}, " +
      s"tags: ${tags.toList}, " +
      s"version: ${version.getOrElse("None")}," +
      s"timestamp: ${timestamp.getOrElse("None")}, " +
      s"changeset: ${changeset.getOrElse("None")}, " +
      s"uid: ${uid.getOrElse("None")}, " +
      s"user: ${user.getOrElse("None")}, " +
      s"visible: ${visible.getOrElse("True")}\n"
  }

  object WayEntityTypes extends Enumeration { // TODO: How to know the type ?????
    val Open, Close, Area, CombinedClosedPolylineArea = Value
  }

}

object WayEntity {

  val DEFAULT_DATE_GRANULARITY: Int = 1000

  def apply(osmosisWay: Way, osmosisStringTable: StringTable): WayEntity = {
    apply(osmosisWay, osmosisStringTable, Option[Int](DEFAULT_DATE_GRANULARITY))
  }

  def apply(osmosisWay: Way,
            osmosisStringTable: StringTable,
            dateGranularity: Option[Int]): WayEntity = {

    val _dateGranularity: Int = dateGranularity.getOrElse(DEFAULT_DATE_GRANULARITY)

    // Calculate nodes references in stored in delta compression.
    // Similar to calculate cumulative sum here, drop the first 0L
    val nodes: Seq[Long] = osmosisWay.refs.scanLeft(0L) { _ + _ }.drop(1)
    val optionalInfo: Option[Info] = osmosisWay.info

    // Calculate tags using the StringTable.
    val tags = (osmosisWay.keys, osmosisWay.vals).zipped.map { (k, v) =>
      osmosisStringTable.s(k).toString(DecompressUtils.STRING_ENCODER) -> osmosisStringTable.s(v).toString(DecompressUtils.STRING_ENCODER)
    }.toMap

    val version: Option[Int] = optionalInfo.filter(_.version.isDefined).map(_.version.get)
    val timestamp: Option[Long] = optionalInfo.filter(_.timestamp.isDefined).map(_.timestamp.get * _dateGranularity)
    val changeset: Option[Long] = optionalInfo.filter(_.changeset.isDefined).map(_.changeset.get)
    val uid: Option[Int] = optionalInfo.filter(_.uid.isDefined).map(_.uid.get)
    val userSid: Option[Int] = optionalInfo.filter(_.userSid.isDefined).map(_.userSid.get)
    val user: Option[String] = userSid.map(usersid => osmosisStringTable.s(usersid).toString(DecompressUtils.STRING_ENCODER))
    val visible: Option[Boolean] = optionalInfo.filter(_.visible.isDefined).map(_.visible.get)

    WayEntity(
      id = osmosisWay.id,
      nodes = nodes,
      tags = tags,
      version = version,
      timestamp = timestamp,
      changeset = changeset,
      uid = uid,
      user = user,
      visible = visible
    )
  }

}
