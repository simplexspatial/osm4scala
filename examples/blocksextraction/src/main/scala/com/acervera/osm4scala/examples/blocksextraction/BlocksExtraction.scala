package com.acervera.osm4scala.examples.blocksextraction

import java.io._
import java.nio.file.{Files, Paths}
import java.util.zip.Inflater

import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.utilities.{PrimitiveGroupType, PrimitiveGroupUtils}
import com.trueaccord.scalapb.GeneratedMessage
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveBlock
import PrimitiveGroupType._

object BlocksExtraction extends PrimitiveGroupUtils {

  def fromPbf(pbfFilePath: String, extractRootFolder: String): Unit = {
    var pbfIS: InputStream = null
    try {
      var counter = 0
      pbfIS = new FileInputStream(pbfFilePath)
      BlobTupleIterator.fromPbf(pbfIS).foreach(x => {
        if (x._1.`type` == "OSMData") {
          val folder = s"$extractRootFolder/$counter"
          Files.createDirectories(Paths.get(folder))
          fromBlob(x._2, s"$extractRootFolder/$counter")
          counter += 1
        }
      })
    } finally {
      if (pbfIS != null) pbfIS.close()
    }
  }

  def fromBlob(blob: Blob, outputFolderPath: String): Unit = {

    def extractUncompressedPrimitiveBlock(blob: Blob) = {
      if (blob.raw.isDefined) {
        PrimitiveBlock parseFrom new DataInputStream(new ByteArrayInputStream(blob.raw.get.toByteArray))
      } else {
        if (blob.zlibData.isDefined) {

          // Uncompress
          val inflater = new Inflater()
          val decompressedData = new Array[Byte](blob.rawSize.get)
          inflater.setInput(blob.zlibData.get.toByteArray)
          inflater.inflate(decompressedData)
          inflater.end()

          PrimitiveBlock parseFrom new DataInputStream(new ByteArrayInputStream(decompressedData))
        } else {
          throw new Exception("Data not found even compressed.")
        }
      }
    }

    /**
      * Function that write all blocks in a sequence of blocks and increment the counter of blocks of this type.
      *
      * @param counters Map with counters per extension
      * @param blocks Protobuffer sequences of messages that content the block
      * @param ext Extenstion used to store the block and used as key in the map of counters.
      * @return Map with counters
      */
    def writeBlocks(counters: Map[String, Long], blocks: Seq[GeneratedMessage], ext:String) =
      blocks.foldLeft(counters)(writeBlock(_ , _, ext))

    /**
      * Function that write a block and increment the counter of blocks of this type.
      *
      * @param counters Map with counters per extension
      * @param block Protobuffer message that content the block
      * @param ext Extenstion used to store the block and used as key in the map of counters.
      * @return Map with counters
      */
    def writeBlock(counters: Map[String, Long], block: GeneratedMessage, ext:String): Map[String, Long] = {
      val output = new FileOutputStream(s"$outputFolderPath/$counters(ext).$ext")
      block writeTo output
      output.close
      counters + (ext-> (counters(ext) + 1L))
    }






    // Read the input stream using DataInputStream to access easily to Int and raw fields. The source could be compressed.
    val primitiveBlock = extractUncompressedPrimitiveBlock(blob)

    val strTableFile = new FileOutputStream(s"$outputFolderPath/strTable")
    primitiveBlock.stringtable writeTo strTableFile
    strTableFile.close

    primitiveBlock.primitivegroup.foldLeft(Map[String, Long]().withDefaultValue(0L))( (counters, primitiveGroup) => {
      detectType(primitiveGroup) match {
        case Relations => writeBlocks(counters, primitiveGroup.relations, "relation")
        case Nodes => writeBlocks(counters, primitiveGroup.nodes, "node")
        case Ways =>  writeBlocks(counters, primitiveGroup.ways, "way")
        case ChangeSets => writeBlocks(counters, primitiveGroup.changesets, "changeset")
        case DenseNodes => writeBlock(counters, primitiveGroup.dense.get , "dense")
        case _ => throw new Exception("Unknown primitive group found.")
      }
    })

  }



}
