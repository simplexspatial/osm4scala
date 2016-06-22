package com.acervera.pbf4scala.model

import java.io.FileInputStream

import org.openstreetmap.osmosis.osmbinary.osmformat.{StringTable, Way}
import org.scalatest.FunSuite


/**
  * Created by angelcervera on 20/06/16.
  */
class WayEntitySuite extends FunSuite {

  test("read a real osmosis Way.") {

    // Read the osmosis string table and way.
    val strTable = StringTable parseFrom new FileInputStream("src/test/resources/com/acervera/pbf4scala/osmblock/ways/8133/strTable")
    val osmosisWay = Way parseFrom new FileInputStream("/home/angelcervera/projects/osm/primitives/spain/8133/280.way")

    // Test
    val way = WayEntity(strTable, osmosisWay)
    assert(way.id === 199785422)
    assert(way.nodes === List(2097786485L, 2097786450L, 2097786416L, 2097786358L))
    assert(way.tags == Map("source" -> "PNOA", "highway" -> "path", "surface" -> "ground"))
  }

}
