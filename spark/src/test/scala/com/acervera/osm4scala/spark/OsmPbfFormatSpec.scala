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

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession, functions => fn}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import scala.util.Random

class OsmPbfFormatSpec extends AnyWordSpec with Matchers with SparkSessionBeforeAfterAll {

  def withTemporalFolder(testCode: File => Any): Unit =
    testCode(
      new File(
        s"target/test_outs/${this.getClass().getCanonicalName()}/${Random.alphanumeric.take(10).mkString.toUpperCase()}"
      )
    )

  val madridPath = "core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf"
  val monacoPath = "core/src/test/resources/com/acervera/osm4scala/monaco-latest.osm.pbf"

  def loadOsmPbf(spark: SparkSession, path: String, tableName: Option[String] = None): DataFrame = {
    val df = spark.sqlContext.read
      .format("osm.pbf")
      .load(path)
      .repartition(cores * 2)
    tableName.foreach(df.createOrReplaceTempView)
    df
  }

  "OsmPbfFormat" should {

    "parsing all only one time" in {
      val entitiesCount = loadOsmPbf(spark, madridPath).count()
      entitiesCount shouldBe 2677227
    }

    "parser correctly" when {
      "is parsing nodes" when {
        "read all columns" in {
          val node171946 = loadOsmPbf(spark, madridPath).filter("id == 171946").collect()(0)
          node171946.getAs[Long]("id") shouldBe 171946L
          node171946.getAs[Byte]("type") shouldBe 0
          node171946.getAs[Double]("latitude") shouldBe (40.42125 +- 0.001)
          node171946.getAs[Double]("longitude") shouldBe (-3.68445 +- 0.001)
          node171946.getAs[Map[String, String]]("tags") shouldBe
            Map("highway" -> "traffic_signals", "crossing" -> "traffic_signals", "crossing_ref" -> "zebra")

          node171946.getAs[Seq[Any]]("nodes") shouldBe Seq.empty
          node171946.getAs[Seq[Any]]("relations") shouldBe Seq.empty
        }

        "read without info" in {
          val node171946 = loadOsmPbf(spark, madridPath).select("id").filter("id == 171946").collect()(0)
          node171946.getAs[Long]("id") shouldBe 171946L
        }

        "read null info" in {
          val node171946 = loadOsmPbf(spark, madridPath).select("id", "info").filter("id == 171946").collect()(0)
          node171946.getAs[Long]("id") shouldBe 171946L
          node171946.getAs[Row]("info") should be(null)

          val testIfNulls = loadOsmPbf(spark, madridPath)
            .select(
              col("id"),
              (
                col("info.version").isNull and
                col("info.timestamp").isNull and
                col("info.changeset").isNull and
                col("info.userId").isNull and
                col("info.userName").isNull and
                col("info.visible").isNull
              ).as("allNulls")
            ).filter("id == 171946").collect()(0)

          testIfNulls.getAs[Long]("id") shouldBe 171946L
          testIfNulls.getAs[Boolean]("allNulls") shouldBe true
        }

      }

      "is parsing ways" in {
        val way3996192 = loadOsmPbf(spark, madridPath).filter("id == 3996192").collect()(0)
        way3996192.getAs[Long]("id") shouldBe 3996192L
        way3996192.getAs[Byte]("type") shouldBe 1
        way3996192.getAs[AnyRef]("latitude") should be(null)
        way3996192.getAs[AnyRef]("longitude") should be(null)
        way3996192.getAs[Map[String, String]]("tags") shouldBe
          Map("name" -> "Plaza de Grecia",
              "highway" -> "primary",
              "lanes" -> "3",
              "source:name" -> "common knowledge",
              "junction" -> "roundabout")

        way3996192.getAs[Seq[Long]]("nodes") shouldBe Seq(20952914L, 2424952617L)
        way3996192.getAs[Seq[Any]]("relations") shouldBe Seq.empty
      }

      "is parsing relations" in {
        val relation55799 = loadOsmPbf(spark, madridPath).filter("id == 55799").collect()(0)
        relation55799.getAs[Long]("id") shouldBe 55799
        relation55799.getAs[Byte]("type") shouldBe 2
        relation55799.getAs[AnyRef]("latitude") should be(null)
        relation55799.getAs[AnyRef]("longitude") should be(null)
        relation55799.getAs[Map[String, String]]("tags") shouldBe
          Map("type" -> "multipolygon", "building" -> "yes")

        relation55799.getAs[Seq[Any]]("nodes") shouldBe Seq.empty
        relation55799.getAs[InternalRow]("relations") shouldBe Seq(
          Row(28775036L, 1, "outer"),
          Row(28775323, 1, "inner")
        )
      }

    }

    "export to other formats" in withTemporalFolder { tmpFolder =>
      val threeExamples = loadOsmPbf(spark, madridPath)
        .filter("id == 55799 || id == 3996192 || id == 171946")
        .orderBy("id")

      threeExamples.write
        .mode(SaveMode.Overwrite)
        .format("orc")
        .save(s"${tmpFolder}/madrid/three")

      val readFromOrc = spark.sqlContext.read
        .format("orc")
        .load(s"${tmpFolder}/madrid/three")
        .orderBy("id")
        .collect()

      val readFromPbf = threeExamples.collect();

      readFromOrc should be(readFromPbf)

    }

    "execute complex queries" when {
      "using dsl" should {
        "count arrays and filter" in {
          val sparkStable = spark
          import sparkStable.implicits._

          loadOsmPbf(spark, madridPath)
            .withColumn("no_of_nodes", fn.size($"nodes"))
            .withColumn("no_of_relations", fn.size($"relations"))
            .withColumn("no_of_tags", fn.size($"tags"))
            .withColumn("both_counter", fn.size($"relations") + fn.size($"tags"))
            .where("(both_counter < 4) AND (no_of_nodes > 2 OR no_of_relations > 2) ")
            .show()
        }
      }
      "using SQL" should {

        "show changes" in {
          loadOsmPbf(spark, monacoPath, Some("monaco_shows"))
          spark.sqlContext
            .sql(
              """
                | select
                |   id, type, info.version, date_format(info.timestamp, "dd-MMM-y kk:mm:ss O") as timestamp
                | from monaco_shows
                | order by info.timestamp desc
                | """.stripMargin
            )
            .show()
        }

        "count all zebras" in {
          loadOsmPbf(spark, madridPath, Some("madrid_shows"))
          spark.sqlContext
            .sql("select count(*) from madrid_shows where array_contains(map_values(tags), 'zebra')")
            .show()
        }
        "extract all keys used in tags" in {
          loadOsmPbf(spark, madridPath, Some("madrid_shows"))
          spark.sqlContext
            .sql("select distinct explode(map_keys(tags)) as tag from madrid_shows where size(tags) > 0 order by tag")
            .show()
        }

        "extract unique list of types" in {
          loadOsmPbf(spark, monacoPath, Some("monaco_shows"))
          spark.sqlContext
            .sql("select distinct(type) as unique_types from monaco_shows order by unique_types")
            .show()
        }

        "extract ways with more nodes" in {
          loadOsmPbf(spark, monacoPath, Some("monaco_shows"))
          spark.sqlContext
            .sql("select id, size(nodes) as size_nodes from monaco_shows where type == 1 order by size_nodes desc")
            .show()
        }

        "extract relations" in {
          loadOsmPbf(spark, monacoPath, Some("monaco_shows"))
          spark.sqlContext
            .sql("select id, relations from monaco_shows where type == 2")
            .show()
        }

      }
    }
  }

}
