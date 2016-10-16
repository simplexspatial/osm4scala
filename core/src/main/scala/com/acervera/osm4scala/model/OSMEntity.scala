package com.acervera.osm4scala.model

object OSMTypes extends Enumeration {
  type osmType = Value
  val Way, Node, Relation = Value
}

trait OSMEntity {

  val osmModel: OSMTypes.Value
  val id: Long
  val tags: Map[String, String]

}


