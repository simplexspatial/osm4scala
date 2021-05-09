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

import com.acervera.osm4scala.model.RelationMemberEntityTypes
import org.apache.spark.sql.types._

object OsmSqlEntity {

  val FIELD_ID = "id"
  val FIELD_TYPE = "type"
  val FIELD_LATITUDE = "latitude"
  val FIELD_LONGITUDE = "longitude"
  val FIELD_NODES = "nodes"
  val FIELD_RELATIONS = "relations"
  val FIELD_TAGS = "tags"
  val FIELD_INFO = "info"
  val FIELD_RELATIONS_ID = "id"
  val FIELD_RELATIONS_TYPE = "relationType"
  val FIELD_RELATIONS_ROLE = "role"
  val FIELD_INFO_VERSION = "version"
  val FIELD_INFO_TIMESTAMP = "timestamp"
  val FIELD_INFO_CHANGESET = "changeset"
  val FIELD_INFO_USER_ID = "userId"
  val FIELD_INFO_USER_NAME = "userName"
  val FIELD_INFO_VISIBLE = "visible"


  val ENTITY_TYPE_NODE: Byte = 0
  val ENTITY_TYPE_WAY: Byte = 1
  val ENTITY_TYPE_RELATION: Byte = 2

  val RELATION_NODE: Byte = 0
  val RELATION_WAY: Byte = 1
  val RELATION_RELATION: Byte = 2
  val RELATION_UNRECOGNIZED: Byte = 3

  def typeFromOsmRelationEntity(relationType: RelationMemberEntityTypes.Value): Byte = relationType match {
    case RelationMemberEntityTypes.Node         => RELATION_NODE
    case RelationMemberEntityTypes.Way          => RELATION_WAY
    case RelationMemberEntityTypes.Relation     => RELATION_RELATION
    case RelationMemberEntityTypes.Unrecognized => RELATION_UNRECOGNIZED
  }

  lazy val relationSchema = StructType(
    Seq(StructField(FIELD_RELATIONS_ID, LongType, false),
        StructField(FIELD_RELATIONS_TYPE, ByteType, false),
        StructField(FIELD_RELATIONS_ROLE, StringType, true))
  )

  lazy val infoSchema = StructType(Seq(
    StructField(FIELD_INFO_VERSION, IntegerType, true),
    StructField(FIELD_INFO_TIMESTAMP, LongType, true),
    StructField(FIELD_INFO_CHANGESET, LongType, true),
    StructField(FIELD_INFO_USER_ID, IntegerType, true),
    StructField(FIELD_INFO_USER_NAME, StringType, true),
    StructField(FIELD_INFO_VISIBLE, BooleanType, true)
  ))


  lazy val schema = StructType(
    Seq(
      StructField(FIELD_ID, LongType, false),
      StructField(FIELD_TYPE, ByteType, false),
      StructField(FIELD_LATITUDE, DoubleType, true),
      StructField(FIELD_LONGITUDE, DoubleType, true),
      StructField(FIELD_NODES, ArrayType(LongType, false), true),
      StructField(FIELD_RELATIONS, ArrayType(relationSchema, false), true),
      StructField(FIELD_TAGS, MapType(StringType, StringType, false), true),
      StructField(FIELD_INFO, StructType(infoSchema), true)
    ))

}
