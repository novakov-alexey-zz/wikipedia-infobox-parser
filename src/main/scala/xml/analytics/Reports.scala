package xml.analytics

import org.apache.spark.sql.SparkSession


object Reports extends App {
  //  val conf = new SparkConf().setAppName("WikiInfoboxReports").setMaster("local[*]")
  //  val sc = new SparkContext(conf)

  val spark = SparkSession.builder().appName("WikiInfoboxReports").config("spark.master", "local[*]").getOrCreate()

  val personDf = spark.read.options(Map(
    "header" -> "true",
    "ignoreLeadingWhiteSpace" -> "true",
    "ignoreTrailingWhiteSpace" -> "true",
    //    "timestampFormat" -> "yyyy-MM-dd HH:mm:ss.SSSZZZ",
    "inferSchema" -> "true",
    "mode" -> "FAILFAST"))
    .csv("/Users/alexey/Downloads/wikipedia/output/person.csv")

  personDf.show(truncate = false, numRows = 100)
}
