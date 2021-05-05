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

package com.acervera.osm4scala.model

import com.acervera.osm4scala.utilities.StringTableUtils._
import org.openstreetmap.osmosis.osmbinary.osmformat

final object OSMTypes extends Enumeration {
  type osmType = Value
  val Way, Node, Relation = Value
}

sealed trait OSMEntity {
  val osmModel: OSMTypes.Value
  val id: Long
  val tags: Map[String, String]
  val info: Info
}

case class Info(
    version: Option[Int] = None,
    timestamp: Option[Long] = None,
    changeset: Option[Long] = None,
    uid: Option[Int] = None,
    userSid: Option[String] = None,
    visible: Option[Boolean] = None
)

object Info {
  def apply(osmosisStringTable: osmformat.StringTable, infoOpt: Option[osmformat.Info]): Info = infoOpt match {
    case None => Info.empty()
    case Some(info) =>
      Info(
        info.version,
        info.timestamp,
        info.changeset,
        info.uid,
        info.userSid.map(idx => osmosisStringTable.getString(idx))
      )
  }

  def empty(): Info = Info()

}

/**
  * Entity that represent a OSM node as https://wiki.openstreetmap.org/wiki/Elements#Node and https://wiki.openstreetmap.org/wiki/Node describe
  */
case class NodeEntity(
    id: Long,
    latitude: Double,
    longitude: Double,
    tags: Map[String, String],
    info: Info = Info.empty()
) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Node
}

/**
  * Entity that represent a OSM way as https://wiki.openstreetmap.org/wiki/Elements#Way and https://wiki.openstreetmap.org/wiki/Way describe
  */
case class WayEntity(
    id: Long,
    nodes: Seq[Long],
    tags: Map[String, String],
    info: Info = Info.empty()
) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Way

  object WayEntityTypes extends Enumeration {
    val Open, Close, Area, CombinedClosedPolylineArea = Value
  }
}

object WayEntity {
  def apply(osmosisStringTable: osmformat.StringTable, osmosisWay: osmformat.Way): WayEntity =
    new WayEntity(
      osmosisWay.id,
      osmosisWay.refs.scanLeft(0L) { _ + _ }.drop(1), // Calculate nodes references in stored in delta compression. TODO: extract to utility class.
      osmosisStringTable.extractTags(osmosisWay.keys, osmosisWay.vals),
      Info(osmosisStringTable, osmosisWay.info)
    )
}

/**
  * Entity that represent a OSM relation as https://wiki.openstreetmap.org/wiki/Elements#Relation and https://wiki.openstreetmap.org/wiki/Relation describe
  */
case class RelationEntity(
    id: Long,
    relations: Seq[RelationMemberEntity],
    tags: Map[String, String],
    info: Info = Info.empty()
) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Relation
}

object RelationEntity {
  def apply(osmosisStringTable: osmformat.StringTable, osmosisRelation: osmformat.Relation): RelationEntity = {

    // Calculate relations
    val relations = (
      osmosisRelation.memids.scanLeft(0L) {_ + _}.drop(1), // Decode members references in stored in delta compression. TODO: extract to utility class.
      osmosisRelation.types,
      osmosisRelation.rolesSid
    ).zipped.map { (m, t, r) =>
      RelationMemberEntity(m, RelationMemberEntityTypes(t.value), osmosisStringTable.getString(r))
    }

    new RelationEntity(
      osmosisRelation.id,
      relations,
      osmosisStringTable.extractTags(osmosisRelation.keys, osmosisRelation.vals),
      Info(osmosisStringTable, osmosisRelation.info)
    )
  }
}

object RelationMemberEntityTypes extends Enumeration {
  type RelationMemberEntityTypes = Value
  val Node = Value(0)
  val Way = Value(1)
  val Relation = Value(2)
  val Unrecognized = Value(3)
}

case class RelationMemberEntity(
    id: Long,
    relationTypes: RelationMemberEntityTypes.Value,
    role: String
)
