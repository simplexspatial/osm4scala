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

import java.io.InputStream

import scala.annotation.tailrec

object OSMDataFinder {
  val pattern = Array[Byte](0x0A, 0x07, 0x4F, 0x53, 0x4D, 0x44, 0x61, 0x74, 0x61)
  val blobHeaderLengthSize = 4

  implicit class InputStreamEnricher(in: InputStream) {

    /**
      * Search the first block. Neive search for this first PoC.
      * If it work, let's try with KMP Algorithm
      *
      */
    def firstBlockIndex(): Long = {
      in.readNBytes(blobHeaderLengthSize) // Ignore length header.

      @tailrec
      def rec(idx: Long, current: Array[Byte]): Long =
        if (current.sameElements(pattern)) idx else rec(idx + 1, current.drop(1) ++ in.readNBytes(1))

      rec(0, in.readNBytes(pattern.size))
    }

  }
}
