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

import com.acervera.osm4scala.utilities.StringTableUtils
import org.openstreetmap.osmosis.osmbinary.osmformat.{Relation, StringTable, Way}

final object OSMTypes extends Enumeration {
  type osmType = Value
  val Way, Node, Relation = Value
}

sealed trait OSMEntity {
  val osmModel: OSMTypes.Value
  val id: Long
  val tags: Map[String, String]

  // Optional metadata
  val version: Option[Int] = None
  val timestamp: Option[Long] = None
  val changeset: Option[Long] = None
  val uid: Option[Int] = None
  val user_sid: Option[String] = None
  val visible: Option[Boolean] = None
}

/**
  * Entity that represent a OSM node as https://wiki.openstreetmap.org/wiki/Elements#Node and https://wiki.openstreetmap.org/wiki/Node describe
  */
case class NodeEntity(id: Long, latitude: Double, longitude: Double, tags: Map[String, String]) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Node
}

/**
  * Entity that represent a OSM way as https://wiki.openstreetmap.org/wiki/Elements#Way and https://wiki.openstreetmap.org/wiki/Way describe
  */
case class WayEntity(id: Long, nodes: Seq[Long], tags: Map[String, String]) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Way

  object WayEntityTypes extends Enumeration {
    val Open, Close, Area, CombinedClosedPolylineArea = Value
  }
}

object WayEntity extends StringTableUtils {
  def apply(osmosisStringTable: StringTable, osmosisWay: Way): WayEntity = {

    // Calculate nodes references in stored in delta compression.
    val nodes = osmosisWay.refs
      .scanLeft(0L) {
        _ + _
      }
      .drop(1)

    new WayEntity(
      osmosisWay.id,
      nodes,
      osmosisStringTable.extractTags(osmosisWay.keys, osmosisWay.vals)
    )
  }
}

/**
  * Entity that represent a OSM relation as https://wiki.openstreetmap.org/wiki/Elements#Relation and https://wiki.openstreetmap.org/wiki/Relation describe
  */
case class RelationEntity(id: Long, relations: Seq[RelationMemberEntity], tags: Map[String, String]) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Relation
}

object RelationEntity extends StringTableUtils {
  def apply(osmosisStringTable: StringTable, osmosisRelation: Relation): RelationEntity = {

    // Decode members references in stored in delta compression.
    val members = osmosisRelation.memids
      .scanLeft(0L) {
        _ + _
      }
      .drop(1)

    // Calculate relations
    val relations = (members, osmosisRelation.types, osmosisRelation.rolesSid).zipped.map { (m, t, r) =>
      RelationMemberEntity(m, RelationMemberEntityTypes(t.value), osmosisStringTable.getString(r))
    }

    new RelationEntity(
      osmosisRelation.id,
      relations,
      osmosisStringTable.extractTags(osmosisRelation.keys, osmosisRelation.vals)
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

case class RelationMemberEntity(id: Long, relationTypes: RelationMemberEntityTypes.Value, role: String)
