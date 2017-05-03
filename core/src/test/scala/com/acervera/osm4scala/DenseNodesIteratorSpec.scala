/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ángel Cervera Claudio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
