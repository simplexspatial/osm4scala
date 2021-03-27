package com.acervera.osm4scala.examples.spark.documentation

import com.acervera.osm4scala.spark.OsmSqlEntity
import org.apache.spark.sql.SparkSession

object PrimitivesCounter {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Primitives counter")
      .getOrCreate()

    spark.read
      .format("osm.pbf")
      .load(args(0))
      .groupBy(OsmSqlEntity.FIELD_TYPE)
      .count
      .show
  }
}
