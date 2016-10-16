package com.acervera.osm4scala

import java.io.{FileInputStream, InputStream}
import java.nio.file.{Files, Paths}

import com.acervera.osm4scala.model.OSMEntity

import scala.io.Source

/**
  * Created by angelcervera on 24/07/16.
  */
object PbfFileIterator {

  /**
    * Create a new PbfFileIterator iterator.
    *
    * @param pbfInputStream Opened InputStream that contains the pbf
    * @return
    */
  def apply(pbfInputStream: InputStream) = new PbfFileIterator(pbfInputStream)

}

/**
  * Iterator over all entities in a pbf file.
  * Every item is a OSMEntity object.
  *
  * @param pbfInputStream Input stream that will be used to read all entities.
  * @author angelcervera
  */
class PbfFileIterator(pbfInputStream: InputStream) extends Iterator[OSMEntity] {

  // Iterator over OSMData blocks
  val blobIterator = BlobIterator(pbfInputStream).withFilter(x => {x._1.`type` == "OSMData"})

  // Iterator entities in active block
  var osmEntitiesIterator : Option[OSMEntitiesIterator] = readNextBlock

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
      Some( OSMEntitiesIterator( blobIterator.next._2 ) )
    } else {
      None
    }

  }

}
