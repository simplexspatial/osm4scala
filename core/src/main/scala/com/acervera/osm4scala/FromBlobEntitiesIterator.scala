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

import com.acervera.osm4scala.model.{NodeEntity, OSMEntity, RelationEntity, WayEntity}
import com.acervera.osm4scala.utilities.Osm4ScalaUtils
import com.acervera.osm4scala.utilities.PrimitiveGroupType._
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.{PrimitiveBlock, PrimitiveGroup}

/**
  * Iterate over all OSMEntities in a FileBlock.
  * The Blob content must be a "OSMData" FileBlock
  */
class FromBlobEntitiesIterator(blob: Blob) extends EntityIterator with Osm4ScalaUtils {

  // Read the input stream using DataInputStream to access easily to Int and raw fields. The source could be compressed.
  private val primitiveBlock = PrimitiveBlock parseFrom dataInputStreamBlob(blob)

  private var primitiveGroupIdx = 0
  private var osmEntityIdx = 0
  private var denseNodesIterator: Option[DenseNodesIterator] = None

  override def hasNext: Boolean = primitiveBlock.primitivegroup.size != primitiveGroupIdx

  /**
    * Move to the next primitive group.
    */
  def nextPrimitiveGroup(): Unit = {
    primitiveGroupIdx += 1
    osmEntityIdx = 0
  }

  /**
    * Extract one relation from the primitive group.
    */
  def extractRelationPrimitiveGroup(currentPrimitiveGroup: PrimitiveGroup): RelationEntity = {
    val currentRelation = currentPrimitiveGroup.relations(osmEntityIdx)

    osmEntityIdx += 1
    if (currentPrimitiveGroup.relations.size == osmEntityIdx) nextPrimitiveGroup()

    RelationEntity(
      currentRelation,
      primitiveBlock.stringtable,
      primitiveBlock.dateGranularity
    )
  }

  /**
    * Extract one way from the primitive group.
    */
  def extractWayPrimitiveGroup(currentPrimitiveGroup: PrimitiveGroup): WayEntity = {
    val currentWay = currentPrimitiveGroup.ways(osmEntityIdx)

    osmEntityIdx += 1
    if (currentPrimitiveGroup.ways.size == osmEntityIdx) nextPrimitiveGroup()

    WayEntity(
      currentWay,
      primitiveBlock.stringtable,
      primitiveBlock.dateGranularity
    )
  }

  /**
    * Extract one node from the dense nodes primitive group.
    */
  def extractDenseNodePrimitiveGroup(currentPrimitiveGroup: PrimitiveGroup): NodeEntity = {
    // If it is the first time, create the iterator.
    if (denseNodesIterator.isEmpty) {
      denseNodesIterator = Some(
        DenseNodesIterator(
          primitiveBlock.stringtable,
          currentPrimitiveGroup.dense.get,
          primitiveBlock.latOffset,
          primitiveBlock.lonOffset,
          primitiveBlock.granularity,
          primitiveBlock.dateGranularity
        )
      )
    }

    // At least, one element.
    val node = denseNodesIterator.get.next()

    if (!denseNodesIterator.get.hasNext) {
      denseNodesIterator = None
      nextPrimitiveGroup()
    }

    node
  }

  override def next(): OSMEntity = {

    val currentPrimitiveGroup: PrimitiveGroup = primitiveBlock.primitivegroup(primitiveGroupIdx)

    // Only one type per primitive group.
    detectType(currentPrimitiveGroup) match {
      case Relations  => extractRelationPrimitiveGroup(currentPrimitiveGroup)
      case Nodes      => throw new NotImplementedError("Nodes does not implemented yet.")
      case Ways       => extractWayPrimitiveGroup(currentPrimitiveGroup)
      case ChangeSets => throw new NotImplementedError("Changeset does not implemented yet")
      case DenseNodes => extractDenseNodePrimitiveGroup(currentPrimitiveGroup)
      case _          => throw new Exception("Unknown primitive group found.")
    }

  }

}
