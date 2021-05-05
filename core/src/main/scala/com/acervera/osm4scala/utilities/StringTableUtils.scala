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

package com.acervera.osm4scala.utilities

import org.openstreetmap.osmosis.osmbinary.osmformat.StringTable

/**
  * Utilities to extract data from StringTable Objects.
  */
private[osm4scala] object StringTableUtils {

  private val CHARSET = "UTF-8"

  implicit class StringTableEnricher(stringTable: StringTable) {

    /**
      * From a sequence of keys and values indexes, it creates a Map of tags.
      *
      * @param keys Sequence of indexes pointing to strings used as keys
      * @param values Sequence of indexes pointing to strings used as values
      * @return Map with tags.
      */
    def extractTags(keys: Seq[Int], values: Seq[Int]): Map[String, String] =
      (keys, values).zipped.map { (key, value) =>
        stringTable.s(key).toString(CHARSET) -> stringTable.s(value).toString(CHARSET)
      }.toMap

    /**
      * From a sequence of indexes following the sequence pattern `(key,value)*`, it creates a Map of tags.
      *
      * @param keyValueSequence key,value sequence.
      * @return Map with tags.
      */
    def extractTags(keyValueSequence: Iterator[Int]): Map[String, String] =
      keyValueSequence
        .grouped(2)
        .map(tag => stringTable.s(tag.head).toString(CHARSET) -> stringTable.s(tag.last).toString(CHARSET))
        .toMap

    /**
      * Extract String from the String table.
      *
      * @param idx String index.
      * @return Proper Scala String.
      */
    def getString(idx: Int): String = stringTable.s(idx).toString(CHARSET)

  }

}
