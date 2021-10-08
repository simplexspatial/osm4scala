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

import com.acervera.osm4scala.EntityIterator._
import com.acervera.osm4scala.model.{NodeEntity, RelationEntity, WayEntity}
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec

import java.io.{FileInputStream, InputStream}

/**
  * Created by angelcervera on 24/07/16.
  */
class FromPbfFileEntitiesIteratorSpec extends AnyWordSpec with Matchers with TableDrivenPropertyChecks {

  val filesAndExpected =
    Table(
      ("testName", "filePath", "total", "nodes", "ways", "relations"),  // First tuple defines column names
      ("Madrid", "core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf",   2677227, 2328075, 338795, 10357),  // Subsequent tuples define the data
      ("ThreeBlocksNodes", "core/src/test/resources/com/acervera/osm4scala/fileblock/three_blocks.pbf",   16000, 16000, 0, 0),  // Subsequent tuples define the data
      // TODO: Add more cases, for ways and relations.
    )

  "FromPbfFileEntitiesIterator" should {
    forAll(filesAndExpected) { (testName, filePath, t, n, w, r) =>
      s"Read X entities in $testName" in {
        val testFile = filePath
        var pbfIS: InputStream = null
        val (nodes, ways, relations, others, total) = try {
          pbfIS = new FileInputStream(testFile)
          fromPbf(pbfIS)
            .foldLeft((0, 0, 0, 0, 0)) {
              case ((nodes, ways, relations, others, total), e) =>
                e match {
                  case _: NodeEntity     => (nodes + 1, ways, relations, others, total + 1)
                  case _: RelationEntity => (nodes, ways, relations + 1, others, total + 1)
                  case _: WayEntity      => (nodes, ways + 1, relations, others, total + 1)
                  case _                 => (nodes, ways, relations, others + 1, total + 1)
                }
            }
        } finally {
          if (pbfIS != null) pbfIS.close()
        }

        total shouldBe t // There are 2.677.227 entities in Madrid!
        nodes shouldBe n // There are 2.328.075 nodes in Madrid!
        ways shouldBe w // There are 338.795 ways in Madrid!
        relations shouldBe r // There are 10.357 relations in Madrid!
        others shouldBe 0 // No different type of entities!

      }
    }

  }

}
