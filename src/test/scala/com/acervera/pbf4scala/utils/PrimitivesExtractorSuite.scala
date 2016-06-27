package com.acervera.pbf4scala.utils

import java.io.{File, FilenameFilter}

import com.acervera.pbf4scala.utils.PrimitivesExtractor._
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by angelcervera on 23/06/16.
  */
class PrimitivesExtractorSuite extends FunSuite with BeforeAndAfter {

  val extractRootFolder = "target/testing/utils/"

  before {
    FileUtils.deleteQuietly(new File(extractRootFolder))
  }

  test("Extracting dense blocks from pbf") {
    val pbfFile = "src/test/resources/com/acervera/pbf4scala/utils/dense_blocks.osm.pbf"
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

  test("Extracting ways blocks from pbf") {
    val pbfFile = "src/test/resources/com/acervera/pbf4scala/utils/relations_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = new File(dir, name).isDirectory
    }).length == 1, "Must extract one blocks.")

    assert( new File(s"$extractRootFolder/0/").list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".relation")
    }).length == 8000, "8000 relations per block.")

  }

  test("Extracting ways relations from pbf") {
    val pbfFile = "src/test/resources/com/acervera/pbf4scala/utils/ways_blocks.osm.pbf"
    fromPbf(pbfFile, extractRootFolder)

    assert( new File(extractRootFolder).list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = new File(dir, name).isDirectory
    }).length == 1, "Must extract one blocks.")

    assert( new File(s"$extractRootFolder/0/").list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".way")
    }).length == 8000, "8000 ways per block.")

  }

}
