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

import java.io.ByteArrayInputStream

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class InputStreamLengthLimitSpec extends AnyWordSpecLike with Matchers {

  private def readNBytes(in: InputStreamLengthLimit, length: Int) = {
    val buffer = Array.fill[Byte](length)(0)
    in.read(buffer)
  }

  "InputStreamLengthLimit" when {
    "mark" should {
      "not support" in {
        val is = new ByteArrayInputStream(Array.fill[Byte](100)(0))
        val sentinel = new InputStreamLengthLimit(is, 50)
        assertThrows[UnsupportedOperationException] {
          sentinel.mark(10)
        }
      }
    }
    "reset" should {
      "not support" in {
        val is = new ByteArrayInputStream(Array.fill[Byte](100)(0))
        val sentinel = new InputStreamLengthLimit(is, 50)
        assertThrows[UnsupportedOperationException] {
          sentinel.reset()
        }
      }
    }
    "continueReading" should {
      "be false after read more than the limit" in {
        val is = new ByteArrayInputStream(Array.fill[Byte](100)(0))
        val sentinel = new InputStreamLengthLimit(is, 50)

        sentinel.continueReading() shouldBe true

        readNBytes(sentinel, 49)
        sentinel.continueReading() shouldBe true

        readNBytes(sentinel, 1)
        sentinel.continueReading() shouldBe false
      }
      "detect the limit using read byte per byte" in {
        val is = new ByteArrayInputStream(Array.fill[Byte](10)(0))
        val sentinel = new InputStreamLengthLimit(is, 3)

        sentinel.continueReading() shouldBe true

        sentinel.read()
        sentinel.read()
        sentinel.continueReading() shouldBe true

        sentinel.read()
        sentinel.continueReading() shouldBe false

      }
      "detect the limit when skipping" in {
        val is = new ByteArrayInputStream(Array.fill[Byte](10)(0))
        val sentinel = new InputStreamLengthLimit(is, 3)

        sentinel.continueReading() shouldBe true

        sentinel.skip(1)
        sentinel.continueReading() shouldBe true

        sentinel.skip(2)
        sentinel.continueReading() shouldBe false
      }
    }
  }
}
