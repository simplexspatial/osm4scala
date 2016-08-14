package com.acervera.pbf4scala

import java.io.{ByteArrayInputStream, DataInputStream}
import java.util.zip.Inflater

import com.acervera.pbf4scala.model.{OSMEntity, RelationEntity, WayEntity}
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveBlock

object OSMEntitiesIterator {

  /**
    * Create a new OSMEntityIterator iterator.
    *
    * @param blob Blob for this file block
    * @return
    */
  def apply(blob: Blob) = new OSMEntitiesIterator(blob)

}


/**
  * Iterate over all OSMEntities in a FileBlock.
  * The Blob content must be a "OSMData" FileBlock
  */
class OSMEntitiesIterator(blob: Blob) extends Iterator[OSMEntity] {

  // Read the input stream using DataInputStream to access easily to Int and raw fields. The source could be compressed.
  val primitiveBlock = {
    if (blob.raw.isDefined) {
      PrimitiveBlock parseFrom new DataInputStream(new ByteArrayInputStream(blob.raw.get.toByteArray))
    } else {
      if (blob.zlibData.isDefined) {

        // Uncompress
        val inflater = new Inflater()
        val decompressedData = new Array[Byte](blob.rawSize.get)
        inflater.setInput(blob.zlibData.get.toByteArray)
        inflater.inflate(decompressedData)
        inflater.end()

        PrimitiveBlock parseFrom new DataInputStream(new ByteArrayInputStream(decompressedData))
      } else {
        throw new Exception("Data not found even compressed.")
      }
    }
  }

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
    if (currentPrimitiveGroup.dense.isDefined) {
      extractDenseNodePrimitiveGroup
    } else {

      if (currentPrimitiveGroup.ways.nonEmpty) {
        extractWayPrimitiveGroup
      } else {

        if (currentPrimitiveGroup.relations.nonEmpty) {
          extractRelationPrimitiveGroup
        } else {

          // Non supported entities.
          if (currentPrimitiveGroup.nodes.nonEmpty) {
            throw new NotImplementedError("Nodes does not implemented yet.")
          } else {
            if (currentPrimitiveGroup.changesets.nonEmpty) {
              throw new NotImplementedError("Changeset does not implemented yet")
            } else {
              throw new Exception("No data found")
            }
          }

        }

      }
    }

  }

}
