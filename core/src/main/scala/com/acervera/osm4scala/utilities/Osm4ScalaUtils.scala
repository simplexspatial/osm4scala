package com.acervera.osm4scala.utilities

import java.io.{ByteArrayInputStream, DataInputStream}
import java.util.zip.Inflater

import org.openstreetmap.osmosis.osmbinary.fileformat.Blob
import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveGroup

object PrimitiveGroupType extends Enumeration {
  type PrimitiveGroupType = Value
  val Relations, Nodes, Ways, ChangeSets, DenseNodes, Unknown = Value
}

/**
  * Utilities to manage primitive groups.
  */
trait Osm4ScalaUtils {


  import PrimitiveGroupType._

  /**
    * Detect the type of group.
    *
    * @param group PrimitiveGroup object too be analysed.
    * @return Group detected ot Unknown if it is Unknown.
    */
  def detectType(group: PrimitiveGroup) : PrimitiveGroupType = group match {
    case _ if group.relations.nonEmpty => Relations
    case _ if group.nodes.nonEmpty => Nodes
    case _ if group.ways.nonEmpty => Ways
    case _ if group.changesets.nonEmpty => ChangeSets
    case _ if group.dense.isDefined => DenseNodes
    case _ => Unknown
  }

  /**
    * Generate an easy readable DataInputStream from a Blob.
    *
    * Because is a DataInputStream, it is really easily to access to Int and raw fields.
    * The source could be compressed, so we decompress it before read.
    * Follow the specification, every Blob can not be more of 32Mb, so we will not have memory problems.
    *
    * @param blob Blob from we extracted and uncompress the content.
    * @return A DataInputStream ready to read.
    */
  def dataInputStreamBlob(blob: Blob): DataInputStream = blob match {
    case _ if blob.raw.isDefined => new DataInputStream(new ByteArrayInputStream(blob.raw.get.toByteArray))
    case _ if blob.zlibData.isDefined => {
      // Uncompress
      val inflater = new Inflater()
      val decompressedData = new Array[Byte](blob.rawSize.get)
      inflater.setInput(blob.zlibData.get.toByteArray)
      inflater.inflate(decompressedData)
      inflater.end()

      new DataInputStream(new ByteArrayInputStream(decompressedData))
    }
    case _ => throw new Exception("Data not found even compressed.")
  }
}
