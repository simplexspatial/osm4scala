package com.acervera.osm4scala

import java.io.InputStream

import com.acervera.osm4scala.model.OSMEntity
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob

/**
  * Iterable process all entities.
  */
trait EntityIterator extends Iterator[OSMEntity]

/**
  * Factory to create EntityIterator objects from different sources.
  */
object EntityIterator {

  /**
    * Create an iterator to iterate over all entities in the InputStream.
    *
    * @param pbfInputStream InputStream object to process
    * @return Iterator
    */
  def fromPbf(pbfInputStream: InputStream) : EntityIterator = new FromPbfFileEntitiesIterator(pbfInputStream)

  /**
    * Create an iterator to iterate over all entities in th Blob.
    *
    * @param blob Blob object to process
    * @return Iterator
    */
  def fromBlob(blob: Blob) : EntityIterator = new FromBlobEntitiesIterator(blob)

}
