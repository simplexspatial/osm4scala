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

package com.acervera.osm4scala.examples.taken

import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.examples.taken.TakeN.fromPbf
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.{File, FileInputStream}

class TakeNSpec extends AnyWordSpecLike with BeforeAndAfterAll with Matchers {

  val tmpFolder = "examples/takeN/target/testing_outputs"
  val monacoInFilePath = "core/src/test/resources/com/acervera/osm4scala/monaco-anonymized.osm.pbf"
  val madridInFilePath = "core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf"

  def counter(file: String): Int = {
    val pbfIS = new FileInputStream(file)
    try {
      BlobTupleIterator.fromPbf(pbfIS).length
    } finally {
      if (pbfIS != null) pbfIS.close()
    }
  }

  "TakeN" should {
    "generate a valid pbf" when {
      "take three blocks" in {
        val outFile = s"$tmpFolder/three.pbf"
        fromPbf(monacoInFilePath, outFile, 3)
        counter(outFile) shouldBe 3
      }
      "take all blocks" in {
        val blocks = counter(monacoInFilePath)
        val outFile = s"$tmpFolder/$blocks.pbf"
        fromPbf(monacoInFilePath, outFile, blocks)
        counter(outFile) shouldBe blocks

        // Compare full file content.
        val original = FileUtils.readFileToByteArray(new File(monacoInFilePath)).toSeq
        val newOne = FileUtils.readFileToByteArray(new File(outFile)).toSeq
        newOne shouldBe(original)
      }
      "take all blocks from bigger one" in {
        val blocks = counter(madridInFilePath)
        val outFile = s"$tmpFolder/$blocks.pbf"
        fromPbf(madridInFilePath, outFile, blocks)
        counter(outFile) shouldBe blocks

        // Compare full file content.
        val original = FileUtils.readFileToByteArray(new File(madridInFilePath)).toSeq
        val newOne = FileUtils.readFileToByteArray(new File(outFile)).toSeq
        newOne shouldBe(original)
      }
    }
  }

  override protected def beforeAll(): Unit = {
    FileUtils.forceMkdir(new File(tmpFolder))
  }

  override protected def afterAll(): Unit = {
    FileUtils.deleteQuietly(new File(tmpFolder))
  }
}
