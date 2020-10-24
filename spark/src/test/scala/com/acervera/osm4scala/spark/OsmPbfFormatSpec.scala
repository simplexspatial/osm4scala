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
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession, functions => fn}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OsmPbfFormatSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  val cores = 4
  val madridPath = "core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf"
  val monacoPath = "core/src/test/resources/com/acervera/osm4scala/monaco-latest.osm.pbf"

  val sparkSession = SparkSession
    .builder()
    .master(s"local[$cores]")
    .getOrCreate()

  import sparkSession.implicits._

  val sqlContext = sparkSession.sqlContext

  def loadOsmPbf(path: String, tableName: Option[String] = None): DataFrame = {
    val df = sqlContext.read
      .format("osm.pbf")
      .load(path)
      .repartition(cores * 2)
    tableName.foreach(df.createTempView)
    df
  }

  override protected def afterAll(): Unit = {
    sparkSession.close()
  }

  "OsmPbfFormat" should {

    "parsing all only one time" in {
      val entitiesCount = loadOsmPbf(madridPath).count()
      entitiesCount shouldBe 2677227
    }

    "parser correctly" when {
      "is parsing nodes" in {
        val node171946 = loadOsmPbf(madridPath).filter("id == 171946").collect()(0)
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
        val way3996192 = loadOsmPbf(madridPath).filter("id == 3996192").collect()(0)
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
        val relation55799 = loadOsmPbf(madridPath).filter("id == 55799").collect()(0)
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

    "export to other formats" in {
      val threeExamples = loadOsmPbf(madridPath)
        .filter("id == 55799 || id == 3996192 || id == 171946")
        .orderBy("id")

      threeExamples.write
        .mode(SaveMode.Overwrite)
        .format("orc")
        .save("target/madrid/three")

      val readFromOrc = sqlContext.read
        .format("orc")
        .load("target/madrid/three")
        .orderBy("id")
        .collect()

      val readFromPbf = threeExamples.collect();

      readFromOrc should be(readFromPbf)

    }

    "execute complex queries" when {
      "using dsl" should {
        "count arrays and filter" in {
          loadOsmPbf(madridPath)
            .withColumn("no_of_nodes", fn.size($"nodes"))
            .withColumn("no_of_relations", fn.size($"relations"))
            .withColumn("no_of_tags", fn.size($"tags"))
            .withColumn("both_counter", fn.size($"relations") + fn.size($"tags"))
            .where("(both_counter < 4) AND (no_of_nodes > 2 OR no_of_relations > 2) ")
            .show()
        }
      }
      "using SQL" should {
        loadOsmPbf(madridPath, Some("madrid_shows"))
        loadOsmPbf(monacoPath, Some("monaco_shows"))
        "count all zebras" in {
          sqlContext
            .sql("select count(*) from madrid_shows where array_contains(map_values(tags), 'zebra')")
            .show()
        }
        "extract all keys used in tags" in {
          sqlContext
            .sql("select distinct explode(map_keys(tags)) as tag from madrid_shows where size(tags) > 0 order by tag")
            .show()
        }

        "extract unique list of types" in {
          sqlContext
            .sql("select distinct(type) as unique_types from monaco_shows order by unique_types")
            .show()
        }

        "extract ways with more nodes" in {
          sqlContext
            .sql("select id, size(nodes) as size_nodes from monaco_shows where type == 1 order by size_nodes desc")
            .show()
        }

        "extract relations" in {
          sqlContext
            .sql("select id, relations from monaco_shows where type == 2")
            .show()
        }

      }
    }
  }

}
