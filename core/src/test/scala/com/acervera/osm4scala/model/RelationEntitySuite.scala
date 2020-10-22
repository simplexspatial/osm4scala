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

import org.openstreetmap.osmosis.osmbinary.osmformat.{Relation, StringTable}
import org.scalatest.funsuite.AnyFunSuite

/**
  * Created by angelcervera on 23/06/16.
  */
class RelationEntitySuite extends AnyFunSuite {

  test("read a real osmosis Relations.") {

    // Read the osmosis string table and way.
    val strTable = StringTable parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/relations/8486/strTable")
    val osmosisRelation = Relation parseFrom new FileInputStream("core/src/test/resources/com/acervera/osm4scala/osmblock/relations/8486/7954.relation")

    // Test
    val relation = RelationEntity(osmosisStringTable = strTable, osmosisRelation = osmosisRelation)
    assert(relation.id === 2898444)
    assert(relation.relations === List(RelationMemberEntity(219042667,RelationMemberEntityTypes.Way,"inner"),RelationMemberEntity(219042634,RelationMemberEntityTypes.Way,"outer")))
    assert(relation.tags == Map("type" -> "multipolygon"))
  }

}
