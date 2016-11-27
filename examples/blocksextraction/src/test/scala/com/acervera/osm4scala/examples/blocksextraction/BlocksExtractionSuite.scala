package com.acervera.osm4scala.examples.blocksextraction

import java.io.{File, FilenameFilter}

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfter, FunSuite}
import BlocksExtraction._

/**
  * Created by angelcervera on 23/06/16.
  */
class BlocksExtractionSuite extends FunSuite with BeforeAndAfter {

  val extractRootFolder = "target/testing/BlocksExtractionSuite/"

  before {
    FileUtils.deleteQuietly(new File(extractRootFolder))
  }

  test("Extracting dense blocks from pbf") {
    val pbfFile = "examples/blocksextraction/src/test/resources/com/acervera/osm4scala/examples/blocksextraction/dense_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list().length == 2, "Must extract two blocks.")
  }

  test("Extracting relations blocks from pbf") {
    val pbfFile = "examples/blocksextraction/src/test/resources/com/acervera/osm4scala/examples/blocksextraction/relations_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list().length == 1, "Must extract one blocks.")
  }

  test("Extracting ways blocks from pbf") {
    val pbfFile = "examples/blocksextraction/src/test/resources/com/acervera/osm4scala/examples/blocksextraction/ways_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list().length == 1, "Must extract one blocks.")
  }

}
