package com.acervera.osm4scala

import java.io.InputStream

import com.acervera.osm4scala.model.OSMEntity

/**
  * Iterator over all entities in a pbf file.
  * Every item is a OSMEntity object.
  *
  * @param pbfInputStream Input stream that will be used to read all entities.
  * @author angelcervera
  */
class FromPbfFileEntitiesIterator(pbfInputStream: InputStream) extends EntityIterator {

  // Iterator over OSMData blocks
  val blobIterator = BlobTupleIterator.fromPbf(pbfInputStream).withFilter(x => {x._1.`type` == "OSMData"})

  // Iterator entities in active block
  var osmEntitiesIterator : Option[EntityIterator] = readNextBlock

  override def hasNext: Boolean = osmEntitiesIterator.isDefined && ( osmEntitiesIterator.get.hasNext || blobIterator.hasNext)

  override def next(): OSMEntity = {
    val nextEntity = osmEntitiesIterator.get.next

    if(!osmEntitiesIterator.get.hasNext) {
      osmEntitiesIterator = readNextBlock
    }

    nextEntity
  }

  /**
    * Read the next osm pbf block
    */
  private def readNextBlock() = {

    if(blobIterator hasNext) {
      Some( EntityIterator.fromBlob( blobIterator.next._2 ) )
    } else {
      None
    }

  }

}
