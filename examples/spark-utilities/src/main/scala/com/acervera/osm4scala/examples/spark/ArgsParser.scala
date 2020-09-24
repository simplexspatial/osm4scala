/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ángel Cervera Claudio
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

import com.acervera.osm4scala.examples.spark.primitivescounter.{PrimitiveCounterCfg, PrimitivesCounterParser}

case class Config(
    job: String = "none",
    counterConfig: Option[PrimitiveCounterCfg] = None
) {
  def updateCounterCfg(fnt: PrimitiveCounterCfg => PrimitiveCounterCfg): Config =
    copy(
      counterConfig = Some(
        fnt(counterConfig.getOrElse(PrimitiveCounterCfg()))
      )
    )
}

class OptionsParser extends scopt.OptionParser[Config]("osm4scala-spark-utilities") with PrimitivesCounterParser {
  head("osm4scala-spark-utilities", "Example of utilities processing OSM Pbf files, using Spark and osm4scala")
  help("help").text("prints this usage text")
  checkConfig { c =>
    c match {
      case Config("none", _)  => failure("No command given.")
      case Config(command, None) => failure(s"Command $command is not valid.")
      case _                  => success
    }
  }
}
