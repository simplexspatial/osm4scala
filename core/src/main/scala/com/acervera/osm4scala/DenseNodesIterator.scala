package com.acervera.osm4scala

import com.acervera.osm4scala.model.NodeEntity
import org.openstreetmap.osmosis.osmbinary.osmformat.{DenseNodes, StringTable}

object DenseNodesIterator {

  def apply(osmosisStringTable: StringTable, osmosisDenseNode: DenseNodes) = new DenseNodesIterator(osmosisStringTable, osmosisDenseNode)

}

/**
  * Iterator over a DenseDataNode block.
  * By default, lanOffset, longOffset and graularity is 0, 0 and 100 because I did not found pbf files with other values.
  *
  * @param osmosisStringTable
  * @param osmosisDenseNode
  * @param latOffset
  * @param lonOffset
  * @param granularity
  */
class DenseNodesIterator(osmosisStringTable: StringTable, osmosisDenseNode: DenseNodes, latOffset: Long = 0, lonOffset: Long = 0, granularity: Int = 100) extends Iterator[NodeEntity] {

  if(osmosisDenseNode.denseinfo.isDefined && !osmosisDenseNode.denseinfo.get.visible.isEmpty) {
    throw new Exception("Only visible nodes are implemented.")
  }

  var idIterator =  osmosisDenseNode.id.toIterator
  var lonIterator = osmosisDenseNode.lon.toIterator
  var latIterator = osmosisDenseNode.lat.toIterator
  var tagsIterator = osmosisDenseNode.keysVals.toIterator

  var lastNode: NodeEntity = NodeEntity(0, 0, 0, Map())

  override def hasNext: Boolean = idIterator hasNext

  override def next(): NodeEntity = {

    val id = idIterator.next() + lastNode.id
    val latitude = decompressCoord(latOffset, latIterator.next(), lastNode.latitude)
    val longitude = decompressCoord(lonOffset, lonIterator.next(), lastNode.longitude)
    val tags = tagsIterator takeWhile( _ != 0l) grouped(2) map {(tag) => osmosisStringTable.s(tag.head).toString("UTF-8") -> osmosisStringTable.s(tag.last).toString("UTF-8")} toMap

    // Create node
    lastNode = NodeEntity(id, latitude, longitude, tags)

    lastNode
  }

  /**
    * Calculate coordinate applying offset, granularity and delta.
    *
    * @param offSet
    * @param delta
    * @param currentValue
    * @return
    */
  def decompressCoord(offSet: Long, delta: Long, currentValue: Double) = {
    (.000000001 * (offSet + (granularity * delta))) + currentValue
  }

}
