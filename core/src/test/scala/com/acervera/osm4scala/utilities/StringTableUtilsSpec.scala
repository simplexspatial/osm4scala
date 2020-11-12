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

package com.acervera.osm4scala.utilities

import com.google.protobuf.ByteString
import org.openstreetmap.osmosis.osmbinary.osmformat.StringTable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StringTableUtilsSpec extends AnyWordSpecLike with Matchers with StringTableUtils {

  val strTable = StringTable(
    (0 to 10).map(idx => ByteString.copyFromUtf8(s"value$idx"))
  )

  "StringTableEnricher" should {
    "extract tags from one sequences of keys and other of values " in {
      strTable.extractTags(Seq(0,5), Seq(2,1)) shouldBe Map(
        "value0" -> "value2",
        "value5" -> "value1",
      )
    }
    "extract tags from key,value sequence" in {
      strTable.extractTags(Seq(0,2,5,1).toIterator) shouldBe Map(
        "value0" -> "value2",
        "value5" -> "value1",
      )
    }
    "extract the String" in {
      strTable.getString(0) shouldBe "value0"
    }
  }

}
