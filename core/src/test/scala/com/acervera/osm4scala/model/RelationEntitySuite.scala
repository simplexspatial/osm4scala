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

import org.openstreetmap.osmosis.osmbinary.osmformat.{Relation, StringTable}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.FileInputStream
import java.time.Instant

/**
  * Created by angelcervera on 23/06/16.
  */
class RelationEntitySuite extends AnyFunSuite with Matchers {

  test("read a real known osmosis Relations.") {

    // Read the osmosis string table and way.
    val strTable = StringTable parseFrom new FileInputStream(
      "core/src/test/resources/com/acervera/osm4scala/primitives/relation/strTable")
    val osmosisRelation = Relation parseFrom new FileInputStream(
      "core/src/test/resources/com/acervera/osm4scala/primitives/relation/relation")

    // Test
    val relation = RelationEntity(strTable, osmosisRelation)


    relation shouldBe RelationEntity(
      11538023L,
      Seq(
        RelationMemberEntity(840127147L, RelationMemberEntityTypes.Way, "outer"),
        RelationMemberEntity(840127148L, RelationMemberEntityTypes.Way, "inner")
      ),
      Map("building" -> "yes", "type" -> "multipolygon"),
      Some(
        Info(
          Some(1),
          Some(Instant.parse("2020-08-24T10:22:47Z")),
          Some(0),
          Some(0),
          Some(""),
          None
        )
      )
    )

  }

}
