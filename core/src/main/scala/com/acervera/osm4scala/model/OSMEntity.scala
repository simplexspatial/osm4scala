package com.acervera.osm4scala.model

trait OSMEntity {

  val osmModel: OSMTypes.Value
  val id: Long
  val tags: Map[String, String]

  object OSMTypes extends Enumeration {
    val Way, Node, Relation = Value
  }

}


