package com.acervera.osm4scala

import java.io.{File, FileInputStream}

import org.openstreetmap.osmosis.osmbinary.osmformat.{DenseNodes, StringTable}
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

class DenseNodesIteratorSpec extends WordSpec with Matchers {

  "The DenseNodesIterator should" should {
    "Read 6432 nodes" in {
      val strTable = StringTable parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/denses/7875/strTable")
      val osmosisDense = DenseNodes parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/denses/7875/0.dense")
      var counter = 0
      DenseNodesIterator(strTable, osmosisDense).foreach(x => counter += 1)
      assert(counter == 6432, "There are 6432 nodes!")
    }

    "Decode location" in {
      val strTable = StringTable parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/denses/7875/strTable")
      val osmosisDense = DenseNodes parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/denses/7875/0.dense")
      val expectedCoordIter = Source fromFile new File("core/src/test/resources/com/acervera/osm4scala/osmblock/denses/7875/nodes_coord_list.txt") getLines()
      DenseNodesIterator(strTable, osmosisDense).foreach(x => {
        val latAndLon = expectedCoordIter next() split (",")
        x.latitude shouldBe latAndLon(0).toDouble +-  0.01
        x.longitude shouldBe latAndLon(1).toDouble +-  0.01
      })
    }
  }

}
