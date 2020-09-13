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

import org.apache.spark.sql.SparkSession
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OsmPbfFormatSpec extends AnyWordSpec with Matchers {

  "OsmPbfFormat" should {
    "parser full file" in {
      val spark = SparkSession
        .builder()
        .master("local[4]")
        .appName("Testing real spark example")
        .getOrCreate()

      val sql = spark.sqlContext
      val madrid = sql.read
        .format("osm.pbf")
        .load("core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf")

      madrid.select("id", "latitude", "longitude").where("type == 0").show()
      madrid.select("id", "latitude", "longitude").where("id == 171933").show()

      madrid.createTempView("madrid")
      sql.sql( "select count(id) from madrid where type == 0").show()
      sql.sql( "select count(*) from madrid").show()

      spark.close()

    }

    "export to other formats" in {
      val spark = SparkSession
        .builder()
        .master("local[4]")
        .appName("Testing real spark example")
        .getOrCreate()

      val sql = spark.sqlContext
      sql.read
        .format("osm.pbf")
        .load("core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf")
        .write
        .format("orc")
        .save("target/madrid/all")

      sql.read
        .format("osm.pbf")
        .load("core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf")
        .select("id", "latitude", "longitude")
        .where("type == 0")
        .write
        .format("csv")
        .save("target/madrid/nodes")

      spark.close()
    }
  }

}
