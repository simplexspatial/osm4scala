import sys
from pyspark.sql import SparkSession

if __name__ == '__main__':

    spark = SparkSession.builder.master("local[*]").appName("Primitives counter").getOrCreate()

    spark.read.format("osm.pbf")\
        .load(sys.argv[1])\
        .groupBy("type")\
        .count()\
        .show()
