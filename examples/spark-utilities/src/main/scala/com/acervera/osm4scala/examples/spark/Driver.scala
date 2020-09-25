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

package com.acervera.osm4scala.examples.spark

import com.acervera.osm4scala.examples.spark.primitivescounter.PrimitivesCounterParser
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object Driver {

  private val OSM_TABLE_NAME = "osm"

  def main(args: Array[String]): Unit = {
    implicit val spark = SparkSession
      .builder()
      .appName("Spark osm4scala examples")
      .getOrCreate()

    new OptionsParser().parse(args, Config()) match {
      case Some(cfg) if cfg.job == PrimitivesCounterParser.CMD_COUNTER => executeJob(cfg, primitivescounter.Job.run)
      case _ =>
    }
  }

  private def executeJob(
                          cfg: Config,
                          fnt: (DataFrame, String, Config) => DataFrame
                        )(implicit sparkSession: SparkSession) = {

    val osmDF = sparkSession.sqlContext.read.format("osm.pbf").load(cfg.inputPath)
    osmDF.createOrReplaceTempView(OSM_TABLE_NAME)

    val resultDF = fnt(osmDF, OSM_TABLE_NAME, cfg)
    val afterCoalesceDF = cfg.coalesce match {
      case None => resultDF
      case Some(c) => resultDF.coalesce(c)
    }
    afterCoalesceDF.write
      .mode(SaveMode.Overwrite)
      .option("header", "true")
      .format(cfg.outputFormat)
      .save(cfg.outputPath)
  }
}
