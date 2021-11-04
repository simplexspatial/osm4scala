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

import com.acervera.osm4scala.model._
import com.acervera.osm4scala.spark.OsmPbfRowIterator._
import com.acervera.osm4scala.spark.OsmSqlEntity._
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.UnsafeArrayData
import org.apache.spark.sql.catalyst.util._
import org.apache.spark.sql.types.{ArrayType, StructField, StructType}
import org.apache.spark.unsafe.types.UTF8String

class OsmPbfRowIterator(osmEntityIterator: Iterator[OSMEntity], requiredSchema: StructType)
    extends Iterator[InternalRow] {

  override def hasNext: Boolean = osmEntityIterator.hasNext
  override def next(): InternalRow = InternalRow.fromSeq(
    osmEntityIterator
      .next()
      .toSQLTypesSeq(requiredSchema)
  )

}

object OsmPbfRowIterator {

  implicit class OsmEntityIterDecorator(osmEntity: Iterator[OSMEntity]) {
    def toOsmPbfRowIterator(structType: StructType): OsmPbfRowIterator = new OsmPbfRowIterator(osmEntity, structType)
  }

  implicit class OsmEntityDecorator(osmEntity: OSMEntity) {

    def toSQLTypesSeq(structType: StructType): Seq[Any] = osmEntity match {
      case entity: NodeEntity     => populateNode(entity, structType)
      case entity: WayEntity      => populateWay(entity, structType)
      case entity: RelationEntity => populateRelation(entity, structType)
    }

    private def populateNode(entity: NodeEntity, structType: StructType): Seq[Any] = structType.fieldNames.map {
      case FIELD_ID        => entity.id
      case FIELD_TYPE      => ENTITY_TYPE_NODE
      case FIELD_LATITUDE  => entity.latitude
      case FIELD_LONGITUDE => entity.longitude
      case FIELD_NODES     => UnsafeArrayData.fromPrimitiveArray(Array.empty[Long])
      case FIELD_RELATIONS => new GenericArrayData(Seq.empty)
      case FIELD_TAGS      => calculateTags(entity.tags)
      case FIELD_INFO      => entity.info.map(populateInfo).orNull
      case fieldName       => throw new Exception(s"Field $fieldName not valid for a Node.")
    }

    private def calculateTags(tags: Map[String, String]): MapData = ArrayBasedMapData(
      tags,
      (k: Any) => UTF8String.fromString(k.toString),
      (v: Any) => UTF8String.fromString(v.toString)
    )

    private def populateInfo(info: Info): InternalRow = InternalRow.fromSeq(infoSchema.fieldNames.map{
      case FIELD_INFO_VERSION   => info.version.getOrElse(null)
      case FIELD_INFO_TIMESTAMP => info.timestamp.map(inst => inst.toEpochMilli * 1000).getOrElse(null)
      case FIELD_INFO_CHANGESET => info.changeset.getOrElse(null)
      case FIELD_INFO_USER_ID   => info.userId.getOrElse(null)
      case FIELD_INFO_USER_NAME => info.userName.map(UTF8String.fromString).orNull
      case FIELD_INFO_VISIBLE   => info.visible.getOrElse(null)
      case fieldName            => throw new Exception(s"Field $fieldName not valid for Info.")
    })

    private def populateWay(entity: WayEntity, structType: StructType): Seq[Any] = structType.fieldNames.map {
      case FIELD_ID        => entity.id
      case FIELD_TYPE      => ENTITY_TYPE_WAY
      case FIELD_LATITUDE  => null
      case FIELD_LONGITUDE => null
      case FIELD_NODES     => UnsafeArrayData.fromPrimitiveArray(entity.nodes.toArray)
      case FIELD_RELATIONS => new GenericArrayData(Seq.empty)
      case FIELD_TAGS      => calculateTags(entity.tags)
      case FIELD_INFO      => entity.info.map(populateInfo).orNull
      case fieldName       => throw new Exception(s"Field $fieldName not valid for a Way.")
    }

    private def populateRelation(entity: RelationEntity, structType: StructType): Seq[Any] =
      structType.fields.map(f =>
        f.name match {
          case FIELD_ID        => entity.id
          case FIELD_TYPE      => ENTITY_TYPE_RELATION
          case FIELD_LATITUDE  => null
          case FIELD_LONGITUDE => null
          case FIELD_NODES     => UnsafeArrayData.fromPrimitiveArray(Seq.empty[Long].toArray)
          case FIELD_RELATIONS => calculateRelations(entity.relations, f)
          case FIELD_TAGS      => calculateTags(entity.tags)
          case FIELD_INFO      => entity.info.map(populateInfo).orNull
          case fieldName       => throw new Exception(s"Field $fieldName not valid for a Relation.")
      })

    private def calculateRelations(relations: Seq[RelationMemberEntity], structField: StructField): ArrayData =
      new GenericArrayData(
        structField.dataType match {
          case ArrayType(elementType, _) =>
            elementType match {
              case s: StructType => relations.map(r => InternalRow.fromSeq(calculateRelation(r, s)))
              case s =>
                throw new UnsupportedOperationException(
                  s"Schema ${s} isn't supported. Only arrays of StructType are allowed for relations.")
            }
          case s =>
            throw new UnsupportedOperationException(
              s"Schema ${s} isn't supported. Only arrays of StructType are allowed for relations.")
        }
      )

    private def calculateRelation(relation: RelationMemberEntity, structType: StructType): Seq[Any] =
      structType.fieldNames.map {
        case FIELD_RELATIONS_ID   => relation.id
        case FIELD_RELATIONS_TYPE => typeFromOsmRelationEntity(relation.relationTypes)
        case FIELD_RELATIONS_ROLE => UTF8String.fromString(relation.role)
        case fieldName            => throw new Exception(s"Field $fieldName not valid for a RelationMember.")
      }

  }

}
