package com.acervera.pbf4scala.model

abstract class OSMEntity {

  val osmModel: OSMTypes.Value
  val id: Long
  val tags: Map[String, String]

  object OSMTypes extends Enumeration {
    val Way, Node, Relation = Value
  }

}


