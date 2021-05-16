/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ángel Cervera Claudio
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

import org.openstreetmap.osmosis.osmbinary.osmformat.{StringTable, Way}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.FileInputStream
import java.time.Instant

/**
  * Created by angelcervera on 20/06/16.
  */
class WayEntitySuite extends AnyFunSuite with Matchers {

  test("read a real osmosis Way.") {

    // Read the osmosis string table and way.
    val strTable = StringTable parseFrom new FileInputStream(
      "core/src/test/resources/com/acervera/osm4scala/primitives/way/strTable")
    val osmosisWay = Way parseFrom new FileInputStream(
      "core/src/test/resources/com/acervera/osm4scala/primitives/way/way")

    // Test
    val way = WayEntity(strTable, osmosisWay)
    way shouldBe WayEntity(
      4097656,
      Vector(
        21912089L, 7265761724L, 1079750744L, 2104793864L, 6340961560L, 1110560507L, 21912093L, 6340961559L, 21912095L,
        7265762803L, 2104793866L, 6340961561L, 5603088200L, 6340961562L, 21912097L, 21912099L
      ),
      Map(
        "name" -> "Avenue Princesse Alice",
        "surface" -> "asphalt",
        "maxspeed" -> "30",
        "highway" -> "primary",
        "lit" -> "yes",
        "lanes" -> "2"
      ),
      Some(
        Info(
          Some(13),
          Some(Instant.parse("2020-03-05T08:50:46Z")),
          Some(0L),
          Some(0),
          Some(""),
          None
        )
      )
    )
  }

}
