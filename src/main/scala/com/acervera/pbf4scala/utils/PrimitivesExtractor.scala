package com.acervera.pbf4scala.utils

import java.io._
import java.nio.file.{Files, Paths}
import java.util.zip.Inflater

import com.acervera.pbf4scala.PbfFileBlockIterator
import com.typesafe.scalalogging.LazyLogging
import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveBlock

object PrimitivesExtractor extends LazyLogging {

  def fromPbf(pbfFilePath: String, extractRootFolder: String): Unit = {
    var pbfIS: InputStream = null
    try {
      var counter = 0
      pbfIS = new FileInputStream(pbfFilePath)
      val iter = PbfFileBlockIterator(pbfIS)
      iter.foreach(x => {
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
    // Read the input stream using DataInputStream to access easily to Int and raw fields. The source could be compressed.
    val primitiveBlock = {
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

    var wayCounter = 0
    var denseCounter = 0
    var nodeCounter = 0
    var relationCounter = 0
    var changeSetCounter = 0

    val strTableFile = new FileOutputStream(s"$outputFolderPath/strTable")
    primitiveBlock.stringtable writeTo strTableFile
    strTableFile.close

    primitiveBlock.primitivegroup.foreach(primitiveGroup => {
      if (primitiveGroup.relations.nonEmpty) {
        logger.debug(s"relations found - ${primitiveGroup.relations.size}")
        primitiveGroup.relations.foreach(relation => {
          val output = new FileOutputStream(s"$outputFolderPath/$relationCounter.relation")
          relation writeTo output
          output.close
          relationCounter += 1
        })
      } else {
        if (primitiveGroup.nodes.nonEmpty) {
          logger.debug(s"nodes found - ${primitiveGroup.nodes.size}")
          primitiveGroup.nodes.foreach(node => {
            val output = new FileOutputStream(s"$outputFolderPath/$nodeCounter.node")
            node writeTo output
            output.close
            nodeCounter += 1
          })
        } else {
          if (primitiveGroup.ways.nonEmpty) {
            logger.debug(s"ways found - ${primitiveGroup.ways.size}")
            primitiveGroup.ways.foreach(way => {
              val output1 = new FileOutputStream(s"$outputFolderPath/$wayCounter.way")
              way writeTo output1
              output1.close
              wayCounter += 1
            })
          } else {
            if (primitiveGroup.changesets.nonEmpty) {
              logger.debug(s"changesets found - ${primitiveGroup.changesets.size}")
              primitiveGroup.changesets.foreach(changeset => {
                val output = new FileOutputStream(s"$outputFolderPath/$changeSetCounter.changeset")
                changeset writeTo output
                output.close
                changeSetCounter += 1
              })
            } else {
              if (primitiveGroup.dense.isDefined) {
                logger.debug(s"dense found")
                val output = new FileOutputStream(s"$outputFolderPath/$denseCounter.dense")
                primitiveGroup.dense.get.writeTo(output)
                output.close
                denseCounter += 1
              } else {
                throw new Exception("No data found")
              }
            }
          }
        }
      }
    })

  }


}
