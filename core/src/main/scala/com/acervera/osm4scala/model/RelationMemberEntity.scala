package com.acervera.osm4scala.model

object RelationMemberEntityTypes extends Enumeration {
  type RelationMemberEntityTypes = Value
  val Node = Value(0)
  val Way = Value(1)
  val Relation = Value(2)
  val Unrecognized = Value(3)
}


case class RelationMemberEntity(val id: Long, val relationTypes: RelationMemberEntityTypes.Value, val role: String)
