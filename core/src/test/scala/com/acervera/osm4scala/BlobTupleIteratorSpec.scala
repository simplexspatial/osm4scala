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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{FileInputStream, InputStream}

class BlobTupleIteratorSpec extends AnyWordSpec with Matchers {

  "The BlobTupleIterator" should {
    "Read three pairs" in {
      val testFile = "core/src/test/resources/com/acervera/osm4scala/fileblock/three_blocks.pbf"
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        BlobTupleIterator.fromPbf(pbfIS).size shouldBe  3
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
    "Read ten pairs" in {
      val testFile = "core/src/test/resources/com/acervera/osm4scala/fileblock/ten_blocks.pbf"
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        BlobTupleIterator.fromPbf(pbfIS).size shouldBe 10
      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
  }

}
