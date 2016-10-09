package com.acervera.osm4scala.model

/**
  * Created by angelcervera on 14/06/16.
  */
case class NodeEntity(val id: Long, val latitude: Double, val longitude: Double, val tags: Map[String, String]) extends OSMEntity {
  override val osmModel: OSMTypes.Value = OSMTypes.Node
}

