/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ángel Cervera Claudio
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

package com.acervera.osm4scala.spark

import com.acervera.osm4scala.model.{OSMTypes, RelationMemberEntity, RelationMemberEntityTypes}
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder

case class OsmSqlEntity(
    id: Long,
    `type`: Byte,
    latitude: Option[Double] = None,
    longitude: Option[Double] = None,
    nodes: Seq[Long] = Seq.empty,
    relations: Seq[OsmSqlRelation] = Seq.empty,
    tags: Map[String, String] = Map.empty
)

case class OsmSqlRelation(id: Long, relationType: Byte, role: String)

object OsmSqlEntity {
  val ENTITY_TYPE_NODE: Byte = 0
  val ENTITY_TYPE_WAY: Byte = 1
  val ENTITY_TYPE_RELATION: Byte = 2

  val RELATION_NODE: Byte = 0
  val RELATION_WAY: Byte = 1
  val RELATION_RELATION: Byte = 2
  val RELATION_UNRECOGNIZED: Byte = 3

  def typeFromOsmEntity(osmType: OSMTypes.Value): Byte = osmType match {
    case OSMTypes.Node     => ENTITY_TYPE_NODE
    case OSMTypes.Way      => ENTITY_TYPE_WAY
    case OSMTypes.Relation => ENTITY_TYPE_RELATION
  }

  def typeFromOsmRelationEntity(relationType: RelationMemberEntityTypes.Value): Byte = relationType match {
    case RelationMemberEntityTypes.Node         => RELATION_NODE
    case RelationMemberEntityTypes.Way          => RELATION_WAY
    case RelationMemberEntityTypes.Relation     => RELATION_RELATION
    case RelationMemberEntityTypes.Unrecognized => RELATION_UNRECOGNIZED
  }

  def relationFromOsmRelationMemberEntity(relation: RelationMemberEntity): OsmSqlRelation = OsmSqlRelation(
    relation.id,
    typeFromOsmRelationEntity(relation.relationTypes),
    relation.role
  )

  lazy val osmEntityEncoder = Encoders.product[OsmSqlEntity]
  lazy val osmEntityExprEncoder = osmEntityEncoder.asInstanceOf[ExpressionEncoder[OsmSqlEntity]]
  lazy val serializer = osmEntityExprEncoder.createSerializer()
  lazy val schema = osmEntityExprEncoder.schema
}
