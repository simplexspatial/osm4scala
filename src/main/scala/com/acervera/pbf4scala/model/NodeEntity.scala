package com.acervera.pbf4scala.model

/**
  * Created by angelcervera on 14/06/16.
  */
case class NodeEntity(val id: Long, val latitude: Long, val longitude: Long, val tags: Map[String, String]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Node

}
