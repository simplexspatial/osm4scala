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

import java.io._
import java.nio.file.{Files, Paths}

import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.utilities.{PrimitiveGroupType, Osm4ScalaUtils}
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveBlock
import PrimitiveGroupType._
import ParametersConfig._
import com.acervera.osm4scala.examples.utilities.Benchmarking

/**
  * Low level example about how to uncompress and extract all primitives to a folder.
  * Usually, it is not necessary to do it, but is good, for example, to extract fragments from the pbf that represent
  * primitives and then use them to test the primitive reader.
  *
  * In this example, I am writing all primitives in different folders belong their string table.
  */
object PrimitivesExtraction extends App with Osm4ScalaUtils with Benchmarking {

  def fromPbf(pbfFilePath: String, extractRootFolder: String): Map[String, Long] = {
    var pbfIS: InputStream = null
    try {
      pbfIS = new FileInputStream(pbfFilePath)
      BlobTupleIterator.fromPbf(pbfIS).foldLeft(Map[String, Long]().withDefaultValue(0L))((counters,x) => {
        if (x._1.`type` == "OSMData") {
          val folder = s"$extractRootFolder/${counters("OSMData")}"
          Files.createDirectories(Paths.get(folder))
          fromBlob(x._2, s"$extractRootFolder/${counters("OSMData")}", counters + ("OSMData"-> (counters("OSMData") + 1L)))
        } else {
          counters
        }
      })
    } finally {
      if (pbfIS != null) pbfIS.close()
    }
  }

  def fromBlob(blob: Blob, outputFolderPath: String, counters: Map[String, Long]): Map[String, Long] = {

    /**
      * Function that write all blocks in a sequence of blocks and increment the counter of blocks of this type.
      *
      * @param counters Map with counters per extension
      * @param primitives Protobuffer sequences of messages that content the block
      * @param ext Extenstion used to store the block and used as key in the map of counters.
      * @return Map with counters
      */
    def writePrimitives(counters: Map[String, Long], primitives: Seq[scalapb.GeneratedMessage], ext:String) =
      primitives.foldLeft(counters)(writePrimitive(_ , _, ext))

    /**
      * Function that write a block and increment the counter of blocks of this type.
      *
      * @param counters Map with counters per extension
      * @param primitive Protobuffer message that content the block
      * @param ext Extenstion used to store the block and used as key in the map of counters.
      * @return Map with counters
      */
    def writePrimitive(counters: Map[String, Long], primitive: scalapb.GeneratedMessage, ext:String): Map[String, Long] = {
      val output = new FileOutputStream(s"$outputFolderPath/${counters(ext)}.${ext}")
      primitive writeTo output
      output.close
      counters + (ext-> (counters(ext) + 1L))
    }





    val primitiveBlock = PrimitiveBlock parseFrom dataInputStreamBlob(blob)

    val strTableFile = new FileOutputStream(s"$outputFolderPath/strTable")
    primitiveBlock.stringtable writeTo strTableFile
    strTableFile.close

    primitiveBlock.primitivegroup.foldLeft(counters)( (counters, primitiveGroup) => {
      detectType(primitiveGroup) match {
        case Relations => writePrimitives(counters, primitiveGroup.relations, "relation")
        case Nodes => writePrimitives(counters, primitiveGroup.nodes, "node")
        case Ways =>  writePrimitives(counters, primitiveGroup.ways, "way")
        case ChangeSets => writePrimitives(counters, primitiveGroup.changesets, "changeset")
        case DenseNodes => writePrimitive(counters, primitiveGroup.dense.get , "dense")
        case _ => throw new Exception("Unknown primitive group found.")
      }
    })

  }

  // Logic that parse parameters.
  parser.parse(args, Config()) match {
    case Some(config) => {
      val result = time { fromPbf(config.input, config.output) }
      println(f"Extracted ${config.input}%s file in ${config.input} in ${result._1 * 1e-9}%,2.2f sec.")
      println(s"Resume: ${result._2}")
    }
    case _ =>
  }


}
