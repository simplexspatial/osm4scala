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

package com.acervera.osm4scala.examples.primitivesextraction

import java.io.{File, FilenameFilter}

import com.acervera.osm4scala.examples.primitivesextraction.PrimitivesExtraction._
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

/**
  * Created by angelcervera on 23/06/16.
  */
class PrimitivesExtractionSuite extends AnyFunSuite with BeforeAndAfter {

  val extractRootFolder = "target/testing_outputs/PrimitivesExtractionSuite/"

  before {
    FileUtils.deleteQuietly(new File(extractRootFolder))
  }

  test("Extracting dense primitives from pbf") {
    val pbfFile = "examples/primitivesextraction/src/test/resources/com/acervera/osm4scala/examples/primitivesextraction/dense_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = new File(dir, name).isDirectory
    }).length == 2, "Must extract two blocks.")

    assert( new File(s"$extractRootFolder/0/").list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".dense")
    }).length == 1, "First block, one dense.")

    assert( new File(s"$extractRootFolder/1/").list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".dense")
    }).length == 1, "Must extract two dense primitives.")
  }

  test("Extracting relations primitives from pbf") {
    val pbfFile = "examples/primitivesextraction/src/test/resources/com/acervera/osm4scala/examples/primitivesextraction/relations_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = new File(dir, name).isDirectory
    }).length == 1, "Must extract one blocks.")

    assert( new File(s"$extractRootFolder/0/").list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".relation")
    }).length == 8000, "8000 relations per block.")

  }

  test("Extracting ways primitives from pbf") {
    val pbfFile = "examples/primitivesextraction/src/test/resources/com/acervera/osm4scala/examples/primitivesextraction/ways_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = new File(dir, name).isDirectory
    }).length == 1, "Must extract one blocks.")

    assert( new File(s"$extractRootFolder/0/").list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".way")
    }).length == 8000, "8000 ways per block.")

  }

}
