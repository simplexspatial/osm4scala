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

package com.acervera.osm4scala.examples.spark.typecounter

import java.io.File

import com.acervera.osm4scala.spark.OsmSqlEntity.{ENTITY_TYPE_NODE, ENTITY_TYPE_RELATION, ENTITY_TYPE_WAY}

/**
  * Command line arguments parser.
  */
trait ParametersConfig {

  val types = Seq("Node", "Way", "Relation")
  def typeFromString(osmType: String): Byte = osmType match {
    case "Node"     => ENTITY_TYPE_NODE
    case "Way"      => ENTITY_TYPE_WAY
    case "Relation" => ENTITY_TYPE_RELATION
  }

  // Parser
  case class Config(inputPath: String = "", outputPath: String = "", outputFormat: String = "", osmType: Option[Byte] = None)
  val parser = new scopt.OptionParser[Config]("spark-count-primitives") {

    opt[String]('i', "input")
      .required()
      .valueName("<file/files>")
      .action((x, c) =>  c.copy(inputPath  = x))
      .text("input is a required pbf2 format set of files")

    opt[String]('o', "output")
      .required()
      .valueName("<path>")
      .action((x, c) =>  c.copy(outputPath  = x))
      .text("output is a required path to store the result")

    opt[String]('f', "outputFormat")
      .required()
      .valueName("[csv, orc, parquet, etc.]")
      .action((x, c) =>  c.copy(outputFormat  = x))
      .text("format that spark will used to store the result")

    opt[String]('t', "type")
      .optional()
      .valueName("<type>")
      .action((x, c) =>  c.copy(osmType  = Some(typeFromString(x))))
      .validate( p => if(types.contains(p)) success else failure("Only [Way, Node, Relation] are supported "))
      .text("primitive type [Way, Node, Relation] used to filter")
  }

}
