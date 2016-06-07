package com.acervera.pbf4scala

import java.io.{FileInputStream, InputStream}

import org.scalatest.WordSpec

class PbfFileBlockIteratorSpec extends WordSpec {

  "The PbfFileBlockIterator should" should {
    "Read three pairs" in {
      val testFile = "src/test/resources/com/acervera/pbf4scala/fileblock/three_blocks.osm.pbf"
      var counter = 0
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        val iter = PbfFileBlockIterator(pbfIS)
        iter.foreach(x => counter += 1)
        assert(counter == 3, "There are 3 blocks!")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
    "Read ten pairs" in {
      val testFile = "src/test/resources/com/acervera/pbf4scala/fileblock/ten_blocks.osm.pbf"
      var counter = 0
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        val iter = PbfFileBlockIterator(pbfIS)
        iter.foreach(x => counter += 1)
        assert(counter == 10, "There are 10 blocks!")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
  }

}
