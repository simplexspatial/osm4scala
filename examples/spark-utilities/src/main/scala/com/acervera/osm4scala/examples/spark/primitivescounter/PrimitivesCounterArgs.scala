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

package com.acervera.osm4scala.examples.spark.primitivescounter

import com.acervera.osm4scala.examples.spark.CommonOptions.{primitiveFromString, primitives}
import com.acervera.osm4scala.examples.spark.primitivescounter.PrimitivesCounterParser.CMD_COUNTER
import com.acervera.osm4scala.examples.spark.{Config, OptionsParser}

case class PrimitiveCounterCfg(
    osmType: Option[Byte] = None
)

object PrimitivesCounterParser {
  val CMD_COUNTER = "counter"
}

trait PrimitivesCounterParser {
  this: OptionsParser =>

  cmd(CMD_COUNTER)
    .action((_, c) => c.copy(job = CMD_COUNTER, counterConfig = Some(PrimitiveCounterCfg())))
    .text("Primitives counter.")
    .children(
      opt[String]('t', "type")
        .optional()
        .valueName("<type>")
        .action {
          case (x, config @ Config(_, _, _, _, _, Some(counterCfg), _)) =>
            config.copy(counterConfig = Some(counterCfg.copy(osmType = Some(primitiveFromString(x)))))
        }
        .validate(p =>
          if (primitives.contains(p)) success else failure(s"Only [${primitives.mkString(", ")}] are supported."))
        .text(s"Primitive type [${primitives.mkString(", ")}] used to filter.")
    )

}
