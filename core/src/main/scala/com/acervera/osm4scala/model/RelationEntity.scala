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

import org.openstreetmap.osmosis.osmbinary.osmformat.{Info, Relation, StringTable}

/**
  * Entity that represent a OSM relation as https://wiki.openstreetmap.org/wiki/Elements#Relation and https://wiki.openstreetmap.org/wiki/Relation describe
  */
case class RelationEntity(id: Long,
                          relations: Seq[RelationMemberEntity],
                          tags: Map[String, String],
                          version: Option[Int],
                          timestamp: Option[Long],
                          changeset: Option[Long],
                          uid: Option[Int],
                          user_sid: Option[Int],
                          visible: Option[Boolean]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Relation

  def apply(id: Long,
            relations: Seq[RelationMemberEntity],
            tags: Map[String, String]): RelationEntity = {
    RelationEntity(id, relations, tags,
      None, None, None, None, None, None)
  }

  override def toString: String = {
    s"Relation id: ${id}, " +
      s"relations: ${relations}, " +
      s"tags: ${tags.toList}, " +
      s"version: ${version.getOrElse("None")}," +
      s"timestamp: ${timestamp.getOrElse("None")}, " +
      s"changeset: ${changeset.getOrElse("None")}, " +
      s"uid: ${uid.getOrElse("None")}, " +
      s"user_sid: ${user_sid.getOrElse("None")}, " +
      s"visible: ${visible.getOrElse("True")}\n"
  }

}

object RelationEntity {

  val DEFAULT_DATE_GRANULARITY: Int = 1000

  def apply(osmosisRelation: Relation, osmosisStringTable: StringTable): RelationEntity = {
    apply(osmosisRelation, osmosisStringTable, Option[Int](DEFAULT_DATE_GRANULARITY))
  }

  def apply(osmosisRelation: Relation,
            osmosisStringTable: StringTable,
            dateGranularity: Option[Int]): RelationEntity = {

    val _dateGranularity: Int = dateGranularity.getOrElse(DEFAULT_DATE_GRANULARITY)

    // Calculate tags using the StringTable.
    val tags: Map[String, String] = (osmosisRelation.keys, osmosisRelation.vals).zipped.map { (k, v) =>
      osmosisStringTable.s(k).toString("UTF-8") -> osmosisStringTable.s(v).toString("UTF-8")
    }.toMap

    // Decode members references in stored in delta compression.
    val members: Seq[Long] = osmosisRelation.memids.scanLeft(0L) { _ + _ }.drop(1)

    val optionalInfo: Option[Info] = osmosisRelation.info

    // Calculate relations
    val relations: Seq[RelationMemberEntity] = (members, osmosisRelation.types, osmosisRelation.rolesSid).zipped.map { (m, t, r) =>
      RelationMemberEntity(m, RelationMemberEntityTypes(t.value), osmosisStringTable.s(r).toString("UTF-8"))
    }

    val version: Option[Int] = optionalInfo.filter(_.version.isDefined).map(_.version.get)
    val timestamp: Option[Long] = optionalInfo.filter(_.timestamp.isDefined).map(_.version.get * _dateGranularity)
    val changeset: Option[Long] = optionalInfo.filter(_.changeset.isDefined).map(_.changeset.get)
    val uid: Option[Int] = optionalInfo.filter(_.uid.isDefined).map(_.uid.get)
    val user_sid: Option[Int] = optionalInfo.filter(_.userSid.isDefined).map(_.userSid.get)
    val visible: Option[Boolean] = optionalInfo.filter(_.visible.isDefined).map(_.visible.get)

    RelationEntity(
      id = osmosisRelation.id,
      relations = relations,
      tags = tags,
      version = version,
      timestamp = timestamp,
      changeset = changeset,
      uid = uid,
      user_sid = user_sid,
      visible = visible
    )
  }

}
