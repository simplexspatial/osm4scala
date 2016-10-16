package com.acervera.osm4scala

import java.io.{FileInputStream, InputStream}

import org.scalatest.WordSpec

class BlobIteratorSpec extends WordSpec {

  "The BlobIterator should" should {
    "Read three pairs" in {
      val testFile = "core/src/test/resources/com/acervera/osm4scala/fileblock/three_blocks.osm.pbf"
      var counter = 0
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        val iter = BlobIterator(pbfIS)
        iter.foreach(x => counter += 1)
        assert(counter == 3, "There are 3 blocks!")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
    "Read ten pairs" in {
      val testFile = "core/src/test/resources/com/acervera/osm4scala/fileblock/ten_blocks.osm.pbf"
      var counter = 0
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        val iter = BlobIterator(pbfIS)
        iter.foreach(x => counter += 1)
        assert(counter == 10, "There are 10 blocks!")
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
  }

}
