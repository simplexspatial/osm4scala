package com.acervera.pbf4scala.model

import java.io.FileInputStream

import org.openstreetmap.osmosis.osmbinary.osmformat.{Relation, StringTable}
import org.scalatest.FunSuite

/**
  * Created by angelcervera on 23/06/16.
  */
class RelationEntitySuite extends FunSuite {

  test("read a real osmosis Relations.") {

    // Read the osmosis string table and way.
    val strTable = StringTable parseFrom new FileInputStream("src/test/resources/com/acervera/pbf4scala/osmblock/relations/8486/strTable")
    val osmosisRelation = Relation parseFrom new FileInputStream("src/test/resources/com/acervera/pbf4scala/osmblock/relations/8486/7954.relation")

    // Test
    val relation = RelationEntity(strTable, osmosisRelation)

    assert(relation.id === 2898444)
    assert(relation.relations === List(RelationMemberEntity(219042667,RelationMemberEntityTypes.Way,"inner"),RelationMemberEntity(219042634,RelationMemberEntityTypes.Way,"outer")))
    assert(relation.tags == Map("type" -> "multipolygon"))
  }

}
