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

object Dependencies {

  lazy val sparkDefaultVersion = spark3Version

//  lazy val selectedScalaVersion = "2.13.10"
  lazy val selectedScalaVersion = "2.12.17"
  //lazy val selectedScalaVersion = "2.11.10"

  lazy val scalatestVersion = "3.2.0"
  lazy val scalacheckVersion = "1.14.3"
  lazy val commonIOVersion = "2.5"
  lazy val logbackVersion = "1.1.7"
  lazy val scoptVersion = "3.7.1"
  lazy val akkaVersion = "2.5.31"
  lazy val spark3Version = "3.3.1"
  lazy val spark2Version = "2.4.8"
}
