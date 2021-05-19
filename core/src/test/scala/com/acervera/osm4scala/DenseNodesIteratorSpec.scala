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

package com.acervera.osm4scala

import com.acervera.osm4scala.model.{Info, NodeEntity}
import org.openstreetmap.osmosis.osmbinary.osmformat.{DenseNodes, StringTable}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.FileInputStream
import java.time.Instant

class DenseNodesIteratorSpec extends AnyWordSpec with Matchers {

  "The DenseNodesIterator should" should {
    "Read known node" in {
      val strTable = StringTable parseFrom new FileInputStream(
        "core/src/test/resources/com/acervera/osm4scala/primitives/dense/strTable"
      )
      val osmosisDense = DenseNodes parseFrom new FileInputStream(
        "core/src/test/resources/com/acervera/osm4scala/primitives/dense/dense"
      )

      DenseNodesIterator(strTable, osmosisDense)
        .find(_.id == 4020124946L) shouldBe Some(
        NodeEntity(
          id = 4020124946L,
          latitude = 43.732560499999984,
          longitude = 7.418018399999998,
          tags = Map("entrance" -> "yes", "addr:street" -> "Rue de la Colle", "addr:housenumber" -> "4"),
          info = Some(
            Info(
              version = Some(1),
              timestamp = Some(Instant.parse("2016-02-22T17:20:29Z")),
              changeset = Some(0),
              userId = Some(0),
              userName = Some(""),
              visible = None
            )
          )
        )
      )
    }
  }

}
