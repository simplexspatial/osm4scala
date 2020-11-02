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

import java.io.File
import java.time.Instant

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession, functions => fn}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

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
  val delawarePath = "core/src/test/resources/com/acervera/osm4scala/delaware-latest.osm.pbf"

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
      "is parsing nodes" in {
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

    "parse optional field correctly" when {
      "parse node with optional fields" in {
        val node75390099 = loadOsmPbf(spark, delawarePath).filter("id == 75390099").collect()(0)
        node75390099.getAs[Long]("id") shouldBe 75390099L
        node75390099.getAs[Byte]("type") shouldBe 0
        node75390099.getAs[Double]("latitude") shouldBe (39.7221284 +- 0.001)
        node75390099.getAs[Double]("longitude") shouldBe (-75.7886029 +- 0.001)
        node75390099.getAs[Map[String, String]]("tags") shouldBe
          Map(
            "ele" -> "240",
            "ref" -> "1",
            "name" -> "Tri-State Marker",
            "historic" -> "boundary_stone",
            "description" -> "Lat-Long (NAD27) \tN39°43'26.3\" W75°47'19.9\"\nUTM (NAD27) \t18S 432415 4397212\nUTM (WGS84) \t18S 432391 4397420",
            "inscription" -> "Top: U.S. COAST & GEODETIC SURVEY REFERENCE MARK / FOR INFORMATION WRITE TO THE DIRECTOR, WASHINGTON, D.C. / $2500 FINE OR IMPRISONMENT FOR DISTURBING THIS MARK. / MD P CORNER NO 2 1935 Side 1: M Side 2: M Side 3: P 1849 Side 4: D"
          )
        node75390099.getAs[Seq[Any]]("nodes") shouldBe Seq.empty
        node75390099.getAs[Seq[Any]]("relations") shouldBe Seq.empty
        node75390099.getAs[Int]("version") shouldBe 9
        node75390099.getAs[Int]("changeset") shouldBe 0
        node75390099.getAs[Long]("timestamp") shouldBe Instant.parse("2020-06-19T02:30:15Z").toEpochMilli
      }
      "parse way with optional fields" in {
        //        loadOsmPbf(delawarePath).filter("type == 1").show(100)
        val way17229816 = loadOsmPbf(spark, delawarePath).filter("id == 17229816").collect()(0)
        way17229816.getAs[Long]("id") shouldBe 17229816L
        way17229816.getAs[Byte]("type") shouldBe 1
        way17229816.getAs[AnyRef]("latitude") shouldBe (null)
        way17229816.getAs[AnyRef]("longitude") shouldBe (null)
        way17229816.getAs[Map[String, String]]("tags") shouldBe
          Map(
            "name" -> "Ridgewood Drive",
            "source" -> "image",
            "highway" -> "residential",
            "maxspeed" -> "25 mph",
            "source_ref" -> "yahoo aerial imagery",
            "tiger:cfcc" -> "A41",
            "tiger:county" -> "New Castle, DE",
            "tiger:zip_left" -> "19707",
            "tiger:name_base" -> "Ridgewood",
            "tiger:name_type" -> "Dr",
            "tiger:zip_right" -> "19707"
          )
        way17229816.getAs[Seq[Long]]("nodes") shouldBe Seq(178634861L, 178682677L, 178682679L, 178682682L, 178682683L,
          178682685L, 178682687L, 178682689L, 178682691L)
        way17229816.getAs[Seq[Any]]("relations") shouldBe Seq.empty
        way17229816.getAs[Int]("version") shouldBe 3
        way17229816.getAs[Int]("changeset") shouldBe 0
        way17229816.getAs[String]("user") shouldBe ""
        way17229816.getAs[Long]("timestamp") shouldBe Instant.parse("2017-09-30T23:45:16Z").toEpochMilli
      }
      "parse relation with optional field" in {
        val relation11777502 = loadOsmPbf(spark, delawarePath).filter("id == 11777502").collect()(0)
        relation11777502.getAs[Long]("id") shouldBe 11777502
        relation11777502.getAs[Byte]("type") shouldBe 2
        relation11777502.getAs[AnyRef]("latitude") should be(null)
        relation11777502.getAs[AnyRef]("longitude") should be(null)
        relation11777502.getAs[Map[String, String]]("tags") shouldBe
          Map("restriction" -> "no_left_turn", "type" -> "restriction")

        relation11777502.getAs[Seq[Any]]("nodes") shouldBe Seq.empty
        relation11777502.getAs[InternalRow]("relations") shouldBe Seq(
          Row(861238019, 1, "from"),
          Row(861238018, 1, "to"),
          Row(178578433, 0, "via")
        )
        relation11777502.getAs[Int]("version") shouldBe 1
        relation11777502.getAs[Long]("timestamp") shouldBe Instant.parse("2020-10-21T01:55:20Z").toEpochMilli
        relation11777502.getAs[Int]("changeset") shouldBe 0
        relation11777502.getAs[String]("user") shouldBe ""
        relation11777502.getAs[Int]("uid") shouldBe 0

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
