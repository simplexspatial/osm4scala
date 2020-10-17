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

import java.io.{FilterInputStream, InputStream}

import com.acervera.osm4scala.InputStreamSentinel

/**
  * Keep in mind that this is not a thread-safe implementation.
  *
  * @param in InputStream to keep count.
  * @param lengthLimit Limit of bytes to read.
  */
class InputStreamLengthLimit(in: InputStream, lengthLimit: Long)
    extends FilterInputStream(in: InputStream) with InputStreamSentinel {

  private var counter = 0L

  override def continueReading(): Boolean = lengthLimit > counter

  override def read(): Int = {
    val result = super.read()
    if (result != -1) counter += 1
    result
  }

  override def read(b: Array[Byte]): Int = {
    val result = super.read(b)
    if (result != -1) counter += result
    result
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    val result = super.read(b, off, len)
    if (result != -1) counter += result
    result
  }

  override def skip(n: Long): Long = {
    val skipped = super.skip(n)
    counter += skipped
    skipped
  }

  override def mark(readlimit: Int): Unit = new UnsupportedOperationException(
    "mark operation is not supported when is wrapped."
  )

  override def reset(): Unit = new UnsupportedOperationException(
    "mark operation is not supported when is wrapped."
  )

  override def markSupported(): Boolean = false

}
