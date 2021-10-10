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

package com.acervera.osm4scala.examples.blocksextraction

import com.acervera.osm4scala.examples.blocksextraction.BlocksExtraction._
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

/**
  * Created by angelcervera on 23/06/16.
  */
class BlocksExtractionSuite extends AnyFunSuite with BeforeAndAfter {

  val extractRootFolder = "examples/blocksextraction/target/testing_outputs/BlocksExtractionSuite/"

  before {
    FileUtils.deleteQuietly(new File(extractRootFolder))
  }

  test("Extracting dense blocks from pbf") {
    val pbfFile = "examples/blocksextraction/src/test/resources/com/acervera/osm4scala/examples/blocksextraction/dense_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list().length == 3*2, "Must extract 3 blocks and headers.")
  }

  test("Extracting relations blocks from pbf") {
    val pbfFile = "examples/blocksextraction/src/test/resources/com/acervera/osm4scala/examples/blocksextraction/relations_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list().length == 2, "Must extract one block and one header.")
  }

  test("Extracting ways blocks from pbf") {
    val pbfFile = "examples/blocksextraction/src/test/resources/com/acervera/osm4scala/examples/blocksextraction/ways_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list().length == 2, "Must extract one block and one header.")
  }

}
