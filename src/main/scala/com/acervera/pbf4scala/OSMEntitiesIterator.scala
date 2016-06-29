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

  override def hasNext: Boolean = primitiveBlock.primitivegroup.size != primitiveGroupIdx

  override def next(): OSMEntity = {

    val currentPrimitiveGroup = primitiveBlock.primitivegroup(primitiveGroupIdx)

    if (currentPrimitiveGroup.ways.nonEmpty) {
      val currentWay = currentPrimitiveGroup.ways(osmEntityIdx)

      osmEntityIdx += 1
      if (currentPrimitiveGroup.ways.size == osmEntityIdx) nextPrimitiveGroup()

      WayEntity(primitiveBlock.stringtable, currentWay)
    } else {
      if (currentPrimitiveGroup.nodes.nonEmpty) {
        throw new NotImplementedError("Nodes does not implemented jet.")
      } else {
        if (currentPrimitiveGroup.relations.nonEmpty) {
          val currentRelation = currentPrimitiveGroup.relations(osmEntityIdx)

          osmEntityIdx += 1
          if (currentPrimitiveGroup.relations.size == osmEntityIdx) nextPrimitiveGroup()

          RelationEntity(primitiveBlock.stringtable, currentRelation)
        } else {
          if (currentPrimitiveGroup.changesets.nonEmpty) {
            throw new NotImplementedError("Changeset does not implemented jet")
          } else {
            if (currentPrimitiveGroup.dense.isDefined) {
              throw new NotImplementedError("Dense does not implemented jet")
            } else {
              throw new Exception("No data found")
            }
          }
        }
      }
    }

  }

  /**
    * Move to the next primitive group.
    */
  def nextPrimitiveGroup() = {
    primitiveGroupIdx += 1
    osmEntityIdx == 0
  }


}
