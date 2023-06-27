/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Ángel Cervera Claudio
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

package com.acervera.osm4scala.utilities

import scala.math.BigDecimal.RoundingMode

/**
 * Utility to manage coordinates
 */
object CoordUtils {

  private val COORDINATE_PRECISION = 7

  /**
   * Calculate coordinate applying offset, granularity and delta.
   * The floating precision is 7 with Half Even rounding
   * mode
   *
   * @param offSet
   * @param delta
   * @param currentValue
   * @return
   */
  def decompressCoord(offSet: Long, delta: Long, currentValue: Double, granularity: Int): Double = {
    BigDecimal.valueOf(offSet + (granularity * delta))
      .*(BigDecimal.valueOf(1E-9))
      .+(BigDecimal.valueOf(currentValue))
      .setScale(COORDINATE_PRECISION, RoundingMode.HALF_EVEN)
      .doubleValue();
  }

  /**
   * Calculate coordinate applying offset, granularity and delta.
   * The floating precision is 7 with Half Even rounding
   *
   * @param coordValue
   * @return
   */
  def convertToMicroDegrees(coordValue: Double): Double = {
    BigDecimal.valueOf(coordValue)
      .*(1E-7)
      .setScale(COORDINATE_PRECISION, RoundingMode.HALF_EVEN)
      .doubleValue();
  }
}
