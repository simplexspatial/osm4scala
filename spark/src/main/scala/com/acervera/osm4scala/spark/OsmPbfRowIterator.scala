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

import com.acervera.osm4scala.model.{NodeEntity, OSMEntity, RelationEntity, WayEntity}
import com.acervera.osm4scala.spark.OsmPbfRowIterator._
import org.apache.spark.sql.catalyst.InternalRow

class OsmPbfRowIterator(osmEntityIterator: Iterator[OSMEntity]) extends Iterator[InternalRow] {

  override def hasNext: Boolean = osmEntityIterator.hasNext
  override def next(): InternalRow = osmEntityIterator.next().toInternalRow()
}

object OsmPbfRowIterator {

  implicit class OsmEntityIteratorDecorator(iter: Iterator[OSMEntity]) {
    def toInternalRowIterator(): Iterator[InternalRow] = new OsmPbfRowIterator(iter)
  }

  implicit class OsmEntityDecorator(osmEntity: OSMEntity) {

    def toOsmSqlEntity(): OsmSqlEntity = osmEntity match {
      case NodeEntity(id, latitude, longitude, tags) =>
        OsmSqlEntity(
          id,
          OsmSqlEntity.ENTITY_TYPE_NODE,
          latitude = Some(latitude),
          longitude = Some(longitude),
          tags = tags
        )
      case WayEntity(id, nodes, tags) =>
        OsmSqlEntity(
          id,
          OsmSqlEntity.ENTITY_TYPE_WAY,
          nodes = nodes,
          tags = tags
        )
      case RelationEntity(id, relations, tags) =>
        OsmSqlEntity(
          id,
          OsmSqlEntity.ENTITY_TYPE_RELATION,
          relations = relations.map(OsmSqlEntity.relationFromOsmRelationMemberEntity),
          tags = tags
        )
    }

    def toInternalRow(): InternalRow = OsmSqlEntity.serializer(osmEntity.toOsmSqlEntity())
  }

}
