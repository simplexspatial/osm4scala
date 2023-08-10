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

package com.acervera.osm4scala.examples.spark.tagkeys

import com.acervera.osm4scala.examples.spark.Config
import com.acervera.osm4scala.examples.spark.SparkSuitesUtilities._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class JobSpec extends AnyWordSpecLike with Matchers {

  for (dataFrame <- Seq(("withoutGeometry", monaco), ("withGeometry", monacoWithGeometry))) {
    "TagKeys" should {
      s"extracting keys without filter - ${dataFrame._1}" in {
        dataFrame._2.createOrReplaceTempView("tag_keys_no_filter")
        val result = Job.run(monaco, "tag_keys_no_filter", Config(tagKeysConfig = Some(TagKeysCfg())))
          .count()
        result shouldBe 833
      }

      s"extracting keys filtering by nodes - ${dataFrame._1}" in {
        dataFrame._2.createOrReplaceTempView("tag_keys_nodes_filter")
        val result = Job.run(monaco, "tag_keys_nodes_filter", Config(tagKeysConfig = Some(TagKeysCfg(osmType = Some(0)))))
          .count()

        result shouldBe 513
      }

      s"extracting keys filtering by ways - ${dataFrame._1}" in {
        dataFrame._2.createOrReplaceTempView("tag_keys_ways_filter")
        val result = Job.run(monaco, "tag_keys_ways_filter", Config(tagKeysConfig = Some(TagKeysCfg(osmType = Some(1)))))
          .count()

        result shouldBe 329
      }

      s"extracting keys filtering by relations - ${dataFrame._1}" in {
        dataFrame._2.createOrReplaceTempView("tag_keys_relations_filter")
        val result = Job.run(monaco, "tag_keys_relations_filter", Config(tagKeysConfig = Some(TagKeysCfg(osmType = Some(2)))))
          .count()

        result shouldBe 485
      }
    }
  }
}
