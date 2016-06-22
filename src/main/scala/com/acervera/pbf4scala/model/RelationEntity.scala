package com.acervera.pbf4scala.model

/**
  * Created by angelcervera on 14/06/16.
  */
case class RelationEntity(val id: Long, val nodes: Seq[Long], val tags: Map[String, String]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Relation

}

