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

package com.acervera.osm4scala.examples.spark.primitivescounter

import com.acervera.osm4scala.examples.spark.Config
import org.apache.spark.sql.{Row, SparkSession}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class JobSpec extends AnyWordSpecLike with BeforeAndAfterAll with Matchers {

  implicit val spark = SparkSession.builder().master("local[4]").getOrCreate()
  val monaco =
    spark.sqlContext.read.format("osm.pbf").load("core/src/test/resources/com/acervera/osm4scala/monaco-latest.osm.pbf")

  override protected def afterAll(): Unit = spark.stop()

  "PrimitiveCounter" should {
    "count without filter" in {
      monaco.createOrReplaceTempView("primitive_counter_no_filter")
      val result = Job.run(monaco, "primitive_counter_no_filter", Config(counterConfig = Some(PrimitiveCounterCfg())))
        .collect()

      result.size shouldBe 3
      result.toSet shouldBe Set(
        Row(0,24726), Row(1,3900), Row(2,242)
      )
    }

    "count filtering by nodes" in {
      monaco.createOrReplaceTempView("primitive_counter_no_filter")
      val result = Job.run(monaco, "primitive_counter_no_filter", Config(counterConfig = Some(PrimitiveCounterCfg(osmType = Some(0)))))
        .collect()

      result.size shouldBe 1
      result.head.get(0) shouldBe 24726
    }

    "count filtering by ways" in {
      monaco.createOrReplaceTempView("primitive_counter_no_filter")
      val result = Job.run(monaco, "primitive_counter_no_filter", Config(counterConfig = Some(PrimitiveCounterCfg(osmType = Some(1)))))
        .collect()

      result.size shouldBe 1
      result.head.get(0) shouldBe 3900
    }

    "count filtering by relations" in {
      monaco.createOrReplaceTempView("primitive_counter_no_filter")
      val result = Job.run(monaco, "primitive_counter_no_filter", Config(counterConfig = Some(PrimitiveCounterCfg(osmType = Some(2)))))
        .collect()

      result.size shouldBe 1
      result.head.get(0) shouldBe 242
    }
  }

}
