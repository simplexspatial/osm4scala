package com.acervera.pbf4scala.model

import org.openstreetmap.osmosis.osmbinary.osmformat.{StringTable, Way}

/**
  * Created by angelcervera on 14/06/16.
  */
case class WayEntity(val id: Long, val nodes: Seq[Long], val tags: Map[String, String]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Way

  object WayEntityTypes extends Enumeration {
    val Open, Close, Area, CombinedClosedPolylineArea = Value
  }

}

object WayEntity {

  def apply(osmosisStringTable: StringTable, osmosisWay: Way) = {

    // Calculate nodes references in stored in delta compression.
    val nodes = osmosisWay.refs.scanLeft(0l) { _ + _ }.drop(1)

    // Calculate tags using the StringTable.
    val tags = (osmosisWay.keys, osmosisWay.vals).zipped.map { (k, v) => osmosisStringTable.s(k).toString("UTF-8") -> osmosisStringTable.s(v).toString("UTF-8") }.toMap


    new WayEntity(osmosisWay.id, nodes, tags)
  }

}
