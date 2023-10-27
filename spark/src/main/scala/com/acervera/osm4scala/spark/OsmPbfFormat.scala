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

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.spark.OSMDataFinder._
import com.acervera.osm4scala.spark.OsmPbfFormat.{PARAMETER_SPLIT, PARAMETER_SPLIT_DEFAULT}
import com.acervera.osm4scala.spark.OsmPbfRowIterator._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, Path}
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.SerializableWritable
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.{FileFormat, OutputWriterFactory, PartitionedFile}
import org.apache.spark.sql.sources.{DataSourceRegister, Filter}
import org.apache.spark.sql.types._
import org.slf4j.{Logger, LoggerFactory}

import java.net.URI

object OsmPbfFormat {
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName.stripSuffix("$"))

  val PARAMETER_SPLIT = "split"
  val PARAMETER_SPLIT_DEFAULT = true
}

/**
  * FileFormat implementation to read [[https://wiki.openstreetmap.org/wiki/PBF_Format OSM Pbf files]].
  *
  * Basically, it is implemented in three steps:
  *   1. Take every split file
  *   1. Search first block of data per split.
  *   2. Extract all entities that are present in the split, starting from the first block found and ending at the last
  *   block whose header is in the split.
  *
  *
  * @example Its usage is like other Spark connectors:
  * {{{
  * scala> spark.read.format("osm.pbf").load("<osm files path here>").select("id","latitude","longitude","tags").filter("type == 0 and size(tags) > 0").show(false)
  * +------+------------------+------------------+-------------------------------------------------------+
  * |id    |latitude          |longitude         |tags                                                   |
  * +------+------------------+------------------+-------------------------------------------------------+
  * |272629|39.01344329999999 |-77.03783400000007|{ref -> 31, highway -> motorway_junction}              |
  * |278454|38.99543909999999 |-76.87885730000008|{noref -> yes, highway -> motorway_junction}           |
  * |278495|39.09727710000001 |-76.8004246000001 |{noref -> yes, highway -> motorway_junction}           |
  * |278499|39.10949280000001 |-76.7842974000001 |{noref -> yes, highway -> motorway_junction}           |
  * |278665|39.13675140000001 |-76.75700080000009|{highway -> motorway_junction, noref -> yes}           |
  * |278679|39.16433720000001 |-76.73629450000011|{noref -> yes, highway -> motorway_junction}           |
  * |278702|39.20996720000001 |-76.68302190000011|{noref -> yes, highway -> motorway_junction}           |
  * |281260|39.047928000000006|-77.15067590000012|{noref -> yes, highway -> motorway_junction}           |
  * |281323|39.1811582        |-77.2515329000001 |{ref -> 15A, highway -> motorway_junction}             |
  * |281359|39.152438         |-77.29614050000009|{highway -> traffic_signals}                           |
  * |287905|38.843457699999995|-77.1106591000001 |{highway -> traffic_signals, traffic_signals -> signal}|
  * |287913|38.85178319999999 |-77.13165830000011|{highway -> traffic_signals}                           |
  * |287943|38.876419799999994|-77.05647270000011|{curve_geometry -> yes}                                |
  * |390841|38.41534949999998 |-77.42648520000014|{ref -> 140, highway -> motorway_junction}             |
  * |390920|38.333087899999974|-77.49828340000015|{ref -> 133, highway -> motorway_junction}             |
  * |390955|38.29694369999998 |-77.50489810000018|{ref -> 130B, highway -> motorway_junction}            |
  * |391002|38.24086209999997 |-77.50026980000017|{ref -> 126B, highway -> motorway_junction}            |
  * |396346|37.97631839999997 |-77.49251700000009|{noref -> yes, highway -> motorway_junction}           |
  * |396542|37.93273709999998 |-77.46779110000014|{ref -> 104, highway -> motorway_junction}             |
  * |396693|37.84187889999999 |-77.45102540000016|{ref -> 98, highway -> motorway_junction}              |
  * +------+------------------+------------------+-------------------------------------------------------+
  * only showing top 20 rows
  * }}}
  *
  * @note Dataframe schema used is:
  *   {{{
  *     root
  *      |-- id: long (nullable = true)
  *      |-- type: byte (nullable = true)
  *      |-- latitude: double (nullable = true)
  *      |-- longitude: double (nullable = true)
  *      |-- nodes: array (nullable = true)
  *      |    |-- element: long (containsNull = true)
  *      |-- relations: array (nullable = true)
  *      |    |-- element: struct (containsNull = true)
  *      |    |    |-- id: long (nullable = true)
  *      |    |    |-- relationType: byte (nullable = true)
  *      |    |    |-- role: string (nullable = true)
  *      |-- tags: map (nullable = true)
  *      |    |-- key: string
  *      |    |-- value: string (valueContainsNull = true)
  *      |-- info: struct (nullable = true)
  *      |    |-- version: integer (nullable = true)
  *      |    |-- timestamp: timestamp (nullable = true)
  *      |    |-- changeset: long (nullable = true)
  *      |    |-- userId: integer (nullable = true)
  *      |    |-- userName: string (nullable = true)
  *      |    |-- visible: boolean (nullable = true)
  *   }}}
  *
  */
class OsmPbfFormat extends FileFormat with DataSourceRegister {

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

  override def isSplitable(sparkSession: SparkSession, options: Map[String, String], path: Path): Boolean =
    options.get(PARAMETER_SPLIT).map(_.toBoolean).getOrElse(PARAMETER_SPLIT_DEFAULT)

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
        val path = new Path(new URI(file.filePath.toString))
        val fs = path.getFileSystem(broadcastedHadoopConf.value.value)
        val status = fs.getFileStatus(path)

        def findFirstBlockOffset(): Option[Long] = {
          var pbfIS: FSDataInputStream = null
          try {
            pbfIS = fs.open(status.getPath)
            pbfIS.seek(file.start)
            pbfIS.firstBlockIndex()
          } finally {
            if (pbfIS != null) pbfIS.close()
          }
        }

        /**
          * Open the file at the specified position.
          *
          * @param offset Initial position.
          * @return Input stream
          */
        def openAtTheBeginning(offset: Long) = {
          val fsIS = fs.open(status.getPath)
          fsIS.seek(file.start + offset)
          fsIS
        }

        findFirstBlockOffset() match {
          case None => Iterator.empty
          case Some(offset) => EntityIterator.fromPbf(
              new InputStreamLengthLimit(
                openAtTheBeginning(offset),
                (file.length - offset) + HEADER_SIZE_LENGTH // plus 4 byte header-size Int
              )
            ).toOsmPbfRowIterator(requiredSchema)
        }

      }
  }

}
