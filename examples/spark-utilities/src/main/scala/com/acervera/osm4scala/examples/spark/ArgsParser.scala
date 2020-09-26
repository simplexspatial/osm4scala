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
import com.acervera.osm4scala.examples.spark.tagkeys.{TagKeysCfg, TagKeysParser}
import com.acervera.osm4scala.spark.OsmSqlEntity.{ENTITY_TYPE_NODE, ENTITY_TYPE_RELATION, ENTITY_TYPE_WAY}

case class Config(
    job: String = "none",
    inputPath: String = "",
    outputPath: String = "",
    outputFormat: String = "",
    coalesce: Option[Int] = None,
    counterConfig: Option[PrimitiveCounterCfg] = None,
    tagKeysConfig: Option[TagKeysCfg] = None
)

object CommonOptions {
  val primitives = Seq("Node", "Way", "Relation")

  def primitiveFromString(osmType: String): Byte = osmType match {
    case "Node"     => ENTITY_TYPE_NODE
    case "Way"      => ENTITY_TYPE_WAY
    case "Relation" => ENTITY_TYPE_RELATION
  }
}

trait CommonOptions {
  this: OptionsParser =>

  head("osm4scala-spark-utilities", "Example of utilities processing OSM Pbf files, using Spark and osm4scala")
  help("help").text("prints this usage text")

  opt[String]('i', "input")
    .required()
    .valueName("<file/files>")
    .action { case (x, cfg) => cfg.copy(inputPath = x) }
    .text("Input is a required pbf2 format set of files.")

  opt[String]('o', "output")
    .required()
    .valueName("<path>")
    .action { case (x, cfg) => cfg.copy(outputPath = x) }
    .text("Output is a required path to store the result.")

  opt[String]('f', "outputFormat")
    .required()
    .valueName("[csv, orc, parquet, etc.]")
    .action { case (x, cfg) => cfg.copy(outputFormat = x) }
    .text("Format that spark will used to store the result.")

  opt[Int]('c', "coalesce")
    .optional()
    .valueName("<coalesce>")
    .action { case (x, cfg) => cfg.copy(coalesce = Some(x)) }
    .text("Number of files that will generate. By default, is not going to join files.")
}

class OptionsParser
    extends scopt.OptionParser[Config]("osm4scala-spark-utilities")
    with CommonOptions
    with PrimitivesCounterParser
    with TagKeysParser {

  checkConfig { c =>
    c match {
      case Config("none", _, _, _, _, _, _)        => failure("No command given.")
      case Config(command, _, _, _, _, None, None) => failure(s"Command $command is not valid.")
      case _                                       => success
    }
  }
}
