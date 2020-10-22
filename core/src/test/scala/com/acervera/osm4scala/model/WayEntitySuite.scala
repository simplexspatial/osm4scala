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

package com.acervera.osm4scala.model

import java.io.FileInputStream

import org.openstreetmap.osmosis.osmbinary.osmformat.{StringTable, Way}
import org.scalatest.funsuite.AnyFunSuite


/**
  * Created by angelcervera on 20/06/16.
  */
class WayEntitySuite extends AnyFunSuite {

  test("read a real osmosis Way.") {

    // Read the osmosis string table and way.
    val strTable = StringTable parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/ways/8133/strTable")
    val osmosisWay = Way parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/ways/8133/280.way")

    // Test
    val way = WayEntity(osmosisStringTable = strTable, osmosisWay = osmosisWay)
    assert(way.id === 199785422)
    assert(way.nodes === List(2097786485L, 2097786450L, 2097786416L, 2097786358L))
    assert(way.tags == Map("source" -> "PNOA", "highway" -> "path", "surface" -> "ground"))
  }

}
