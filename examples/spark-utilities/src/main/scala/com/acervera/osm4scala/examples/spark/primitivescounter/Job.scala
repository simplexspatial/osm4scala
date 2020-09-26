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
import org.apache.spark.sql.{DataFrame, SparkSession}

object Job {
  def run(osmData: DataFrame, tableName: String, cfg: Config)(implicit sparkSession: SparkSession): DataFrame =
    cfg.counterConfig match {
      case None =>
        throw new IllegalArgumentException("Primitive counter called with Primitive counter configuration!!!")
      case Some(primitiveCounterCfg) =>
        primitiveCounterCfg.osmType match {
          case Some(t) =>
            sparkSession.sql(s"select count(*) as num_primitives from ${tableName} where type == ${t}")
          case None =>
            sparkSession.sql(s"select type, count(*) as num_primitives from ${tableName} group by type")
        }
    }
}
