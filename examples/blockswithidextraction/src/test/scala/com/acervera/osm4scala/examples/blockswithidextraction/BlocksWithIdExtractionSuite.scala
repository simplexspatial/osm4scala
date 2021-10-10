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

package com.acervera.osm4scala.examples.blockswithidextraction

import com.acervera.osm4scala.examples.blockswithidextraction.BlocksWithIdExtraction._
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

/**
  * Created by angelcervera on 23/06/16.
  */
class BlocksWithIdExtractionSuite extends AnyFunSuite with BeforeAndAfter {

  val extractRootFolder = "examples/blockswithidextraction/target/testing_outputs/BlocksExtractionSuite/"

  before {
    FileUtils.deleteQuietly(new File(extractRootFolder))
  }

  test("Id found in dense blocks") {
    val pbfFile = "examples/blockswithidextraction/src/test/resources/com/acervera/osm4scala/examples/blockswithidextraction/dense_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder, 14092081)

    assert( new File(extractRootFolder).list().length == 2, "Must extract 1 block and header.")
    new File(extractRootFolder).list().foreach( name => assert(name.startsWith("2_OSMData."), "should be second block"))
  }

  test("Id not found in dense blocks") {
    val pbfFile = "examples/blockswithidextraction/src/test/resources/com/acervera/osm4scala/examples/blockswithidextraction/dense_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder, 0)

    assert( new File(extractRootFolder).list().length == 0)
  }

  test("Id in relations blocks") {
    val pbfFile = "examples/blockswithidextraction/src/test/resources/com/acervera/osm4scala/examples/blockswithidextraction/relations_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder, 3421006)

    assert( new File(extractRootFolder).list().length == 2, "Must extract one block and one header.")
    new File(extractRootFolder).list().foreach( name => assert(name.startsWith("0_OSMData."), "should be first block"))
  }

  test("Id not in relations blocks") {
    val pbfFile = "examples/blockswithidextraction/src/test/resources/com/acervera/osm4scala/examples/blockswithidextraction/relations_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder, 0)

    assert( new File(extractRootFolder).list().length == 0)
  }

  test("Id in ways blocks") {
    val pbfFile = "examples/blockswithidextraction/src/test/resources/com/acervera/osm4scala/examples/blockswithidextraction/ways_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder, 23778140)

    assert( new File(extractRootFolder).list().length == 2, "Must extract one block and one header.")
    new File(extractRootFolder).list().foreach( name => assert(name.startsWith("0_OSMData."), "should be first block"))
  }


  test("Id not in ways blocks") {
    val pbfFile = "examples/blockswithidextraction/src/test/resources/com/acervera/osm4scala/examples/blockswithidextraction/ways_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder, 0)

    assert( new File(extractRootFolder).list().length == 0)
  }

}
