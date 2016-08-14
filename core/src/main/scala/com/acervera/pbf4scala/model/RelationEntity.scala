package com.acervera.pbf4scala.model

import org.openstreetmap.osmosis.osmbinary.osmformat.{Relation, StringTable}


/**
  * Created by angelcervera on 14/06/16.
  */
case class RelationEntity(val id: Long, val relations: Seq[RelationMemberEntity], val tags: Map[String, String]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Relation

}

object RelationEntity {

  def apply(osmosisStringTable: StringTable, osmosisRelation: Relation) = {

    // Calculate tags using the StringTable.
    val tags = (osmosisRelation.keys, osmosisRelation.vals).zipped.map { (k, v) => osmosisStringTable.s(k).toString("UTF-8") -> osmosisStringTable.s(v).toString("UTF-8") }.toMap

    // Decode members references in stored in delta compression.
    val members = osmosisRelation.memids.scanLeft(0l) { _ + _ }.drop(1)

    // Calculate relations
    val relations = (members, osmosisRelation.types, osmosisRelation.rolesSid).zipped.map { (m,t,r) => RelationMemberEntity(m,RelationMemberEntityTypes(t.value),osmosisStringTable.s(r).toString("UTF-8")) }

    new RelationEntity(osmosisRelation.id, relations, tags)
  }

}

