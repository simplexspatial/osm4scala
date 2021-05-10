/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ángel Cervera Claudio
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

package com.acervera.osm4scala.spark

import com.acervera.osm4scala.spark.OSMDataFinder._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.{ByteArrayInputStream, FileInputStream, InputStream}

class OSMDataFinderSpec extends AnyWordSpecLike with Matchers {
  "OSMDataFinder" should {
    "don't find the block" when {
      "file is empty" in {
        val idx = new ByteArrayInputStream(Array.emptyByteArray).firstBlockIndex()
        idx shouldBe None
      }
      "file is larger than block size" in {
        val idx = new ByteArrayInputStream(Array.fill[Byte](4)(0)).firstBlockIndex()
        idx shouldBe None
      }
      "file is smaller than pattern" in {
        val idx = new ByteArrayInputStream(Array.fill[Byte](8)(0)).firstBlockIndex()
        idx shouldBe None
      }
      "file does not contain the pattern" in {
        val idx = new ByteArrayInputStream(Array.fill[Byte](1024 * 10)(0)).firstBlockIndex()
        idx shouldBe None
      }
    }
    "find the block" when {
      "it is the starting chunk" in {
        val testFile = "spark/src/test/resources/com/acervera/osm4scala/spark/splitted/madrid_00"
        var pbfIS: InputStream = null
        try {
          pbfIS = new FileInputStream(testFile)
          val idx = pbfIS.firstBlockIndex()
          idx shouldBe Some(132)
        } finally {
          if (pbfIS != null) pbfIS.close()
        }
      }
      "it is not the starting chunk" in {
        val testFile = "spark/src/test/resources/com/acervera/osm4scala/spark/splitted/madrid_01"
        var pbfIS: InputStream = null
        try {
          pbfIS = new FileInputStream(testFile)
          val idx = pbfIS.firstBlockIndex()
          idx shouldBe Some(34858)
        } finally {
          if (pbfIS != null) pbfIS.close()
        }
      }
    }
  }
}
