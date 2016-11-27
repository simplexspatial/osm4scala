package com.acervera.osm4scala.examples.primitivesextraction

import java.io.{File, FilenameFilter}

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.acervera.osm4scala.examples.primitivesextraction.PrimitivesExtraction._

/**
  * Created by angelcervera on 23/06/16.
  */
class PrimitivesExtractionSuite extends FunSuite with BeforeAndAfter {

  val extractRootFolder = "target/testing/PrimitivesExtractionSuite/"

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
