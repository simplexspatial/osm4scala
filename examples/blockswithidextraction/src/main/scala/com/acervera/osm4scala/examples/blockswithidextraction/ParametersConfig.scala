/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ángel Cervera Claudio
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

package com.acervera.osm4scala.examples.blockswithidextraction

import scopt.OptionParser

/**
  * Command line arguments parser.
  */
object ParametersConfig {

  // Parser
  case class Config(source: String = "", output: String = "", id: Long = 0)
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("extract-blocks-with-ids") {

    opt[String]('s', "source")
      .required()
      .valueName("<file>")
      .action((x, c) => c.copy(source = x))
      .text("source is a required pbf2 format file")

    opt[String]('o', "output")
      .required()
      .valueName("<folder>")
      .action((x, c) => c.copy(output = x))
      .text("output is a required folder path to store blocks extracted.")

    opt[Long]('i', "id")
      .required()
      .valueName("<id>")
      .action((x, c) => c.copy(id = x))
      .text("ID that should be in the block.")

  }

}
