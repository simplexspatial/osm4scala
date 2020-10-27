/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ángel Cervera Claudio
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

package com.acervera.osm4scala.spark

import java.net.URI

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.spark.OSMDataFinder._
import com.acervera.osm4scala.spark.OsmPbfRowIterator._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, Path}
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.SerializableWritable
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.{FileFormat, OutputWriterFactory, PartitionedFile}
import org.apache.spark.sql.sources.{DataSourceRegister, Filter}
import org.apache.spark.sql.types._

class OsmPbfFormat extends FileFormat with DataSourceRegister with Logging {

  override def shortName(): String = "osm.pbf"

  override def inferSchema(sparkSession: SparkSession,
                           options: Map[String, String],
                           files: Seq[FileStatus]): Option[StructType] = Some(OsmSqlEntity.schema)

  override def prepareWrite(sparkSession: SparkSession,
                            job: Job,
                            options: Map[String, String],
                            dataSchema: StructType): OutputWriterFactory =
    throw new UnsupportedOperationException(
      s"write is not supported for spark-osm-pbf files. If you need it, please create a issue and try to support the project."
    )

  override def isSplitable(sparkSession: SparkSession, options: Map[String, String], path: Path): Boolean = true

  override protected def buildReader(sparkSession: SparkSession,
                                     dataSchema: StructType,
                                     partitionSchema: StructType,
                                     requiredSchema: StructType,
                                     filters: Seq[Filter],
                                     options: Map[String, String],
                                     hadoopConf: Configuration): PartitionedFile => Iterator[InternalRow] = {

//    TODO: OsmSqlEntity.validateSchema(requiredSchema)

    val broadcastedHadoopConf = sparkSession.sparkContext.broadcast(new SerializableWritable(hadoopConf))

    (file: PartitionedFile) =>
      {
        val path = new Path(new URI(file.filePath))
        val fs = path.getFileSystem(broadcastedHadoopConf.value.value)
        val status = fs.getFileStatus(path)

        def firstBlockOffset(): Option[Long] = {
          var pbfIS: FSDataInputStream = null
          try {
            pbfIS = fs.open(status.getPath)
            pbfIS.seek(file.start)
            pbfIS.firstBlockIndex()
          } finally {
            if (pbfIS != null) pbfIS.close()
          }
        }

        firstBlockOffset() match {
          case None => Iterator.empty
          case Some(offset) =>
            val atFirstBlock = fs.open(status.getPath)
            atFirstBlock.seek(file.start + offset)
            EntityIterator.fromPbf(new InputStreamLengthLimit(atFirstBlock, file.length - offset)).toOsmPbfRowIterator(requiredSchema)
        }

      }
  }

}
