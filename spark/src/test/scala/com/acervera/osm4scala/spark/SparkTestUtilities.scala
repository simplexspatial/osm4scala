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

package com.acervera.osm4scala.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.scalatest.{BeforeAndAfterAll, Suite}

object SparkSessionFixture {

  /**
    * Create a new spark session.
    * Warning 1: It there is a session open, is going to use it and close it at the end of the test.
    * Warning 2: Using it only with `in` case, don't with `should`, `when`, etc... because the finally is executed before
    * the test. Maybe is executed concurrently.
    *
    * @param cores Number of cores to use.
    * @param appName Application name to use.
    * @param testCode Test to execute
    */
  def withSparkSession(cores: Int, appName: String)(testCode: (SparkSession, SQLContext) => Any): Unit = {
    val sparkSession = SparkSession
      .builder()
      .appName(appName)
      .master(s"local[$cores]")
      .getOrCreate()
    try {
      testCode(sparkSession, sparkSession.sqlContext)
    } finally {
      sparkSession.close()
    }
  }

}

trait SparkSessionBeforeAfterAll extends BeforeAndAfterAll { this: Suite =>

  val cores: Int = 4
  val appName: String = this.getClass().getCanonicalName()

  var spark: SparkSession = _

  def sparkConf(): SparkConf =
    new SparkConf()
      .setAppName(appName)
      .setMaster(s"local[$cores]")

  override def beforeAll(): Unit = {
    spark = SparkSession
      .builder()
      .config(sparkConf())
      .getOrCreate()

    super.beforeAll()
  }

  override def afterAll() {
    try super.afterAll()
    finally {
      spark.close()
    }
  }
}
