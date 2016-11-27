package com.acervera.osm4scala

import com.acervera.osm4scala.model.{OSMEntity, RelationEntity, WayEntity}
import com.acervera.osm4scala.utilities.PrimitiveGroupType._
import com.acervera.osm4scala.utilities.Osm4ScalaUtils
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveBlock

/**
  * Iterate over all OSMEntities in a FileBlock.
  * The Blob content must be a "OSMData" FileBlock
  */
class FromBlobEntitiesIterator(blob: Blob) extends EntityIterator with Osm4ScalaUtils {

  // Read the input stream using DataInputStream to access easily to Int and raw fields. The source could be compressed.
  val primitiveBlock = PrimitiveBlock parseFrom dataInputStreamBlob(blob)

  var primitiveGroupIdx = 0
  var osmEntityIdx = 0
  var denseNodesIterator : Option[DenseNodesIterator] = None

  override def hasNext: Boolean = primitiveBlock.primitivegroup.size != primitiveGroupIdx

  override def next(): OSMEntity = {

    val currentPrimitiveGroup = primitiveBlock.primitivegroup(primitiveGroupIdx)

    /**
      * Move to the next primitive group.
      */
    def nextPrimitiveGroup() = {
      primitiveGroupIdx += 1
      osmEntityIdx = 0
    }

    /**
      * Extract one relation from the primitive group.
      */
    def extractRelationPrimitiveGroup: RelationEntity = {
      val currentRelation = currentPrimitiveGroup.relations(osmEntityIdx)

      osmEntityIdx += 1
      if (currentPrimitiveGroup.relations.size == osmEntityIdx) nextPrimitiveGroup

      RelationEntity(primitiveBlock.stringtable, currentRelation)
    }

    /**
      * Extract one way from the primitive group.
      */
    def extractWayPrimitiveGroup() = {
      val currentWay = currentPrimitiveGroup.ways(osmEntityIdx)

      osmEntityIdx += 1
      if (currentPrimitiveGroup.ways.size == osmEntityIdx) nextPrimitiveGroup

      WayEntity(primitiveBlock.stringtable, currentWay)
    }

    /**
      * Extract one node from the densde nodes primitive group.
      */
    def extractDenseNodePrimitiveGroup() = {
      // If it is the first time, create the iterator.
      if(!denseNodesIterator.isDefined) {
        denseNodesIterator = Some( DenseNodesIterator(primitiveBlock.stringtable, currentPrimitiveGroup.dense.get) )
      }

      // At least, one element.
      val node = denseNodesIterator.get.next

      if(!denseNodesIterator.get.hasNext) {
        denseNodesIterator = None
        nextPrimitiveGroup
      }

      node
    }

    // Only one type per primitive group.
    detectType(currentPrimitiveGroup) match {
      case Relations => extractRelationPrimitiveGroup
      case Nodes => throw new NotImplementedError("Nodes does not implemented yet.")
      case Ways =>  extractWayPrimitiveGroup
      case ChangeSets => throw new NotImplementedError("Changeset does not implemented yet")
      case DenseNodes => extractDenseNodePrimitiveGroup
      case _ => throw new Exception("Unknown primitive group found.")
    }

  }

}
