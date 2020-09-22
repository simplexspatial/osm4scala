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

package com.acervera.osm4scala.examples.spark.typecounter

import org.apache.spark.sql.SparkSession

object Job extends ParametersConfig {

  private def executeQuery(input: String, output: String, outputFormat: String, osmType: Option[Byte])(implicit sparkSession: SparkSession) = {
    import sparkSession.implicits._
    import org.apache.spark.sql.functions._

    val table = sparkSession
      .sqlContext
      .read
      .format("osm.pbf")
      .load(input)
      .createTempView("madrid")

    val noFilter = sparkSession
      .sqlContext
      .read
      .format("osm.pbf")
      .load(input)
      .select($"type")

    val applyFilter = osmType.map(t => noFilter.filter($"type" === t)).getOrElse(noFilter)

    applyFilter
      .groupBy($"type")
      .agg(count("*"))
      .coalesce(1)
      .write
      .format(outputFormat)
      .save(output)
  }

  def run(args: Array[String])(implicit sparkSession: SparkSession): Unit = {
    parser.parse(args, Config()) match {
      case Some(Config(inputPath, outputPath, outputFormat, osmType)) => executeQuery(inputPath, outputPath, outputFormat, osmType)
      case None =>
    }
  }

}
