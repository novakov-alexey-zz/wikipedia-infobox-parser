package xml.analytics

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Reports extends App {
  val spark = SparkSession.builder().appName("WikiInfoboxReports").config("spark.master", "local[*]").getOrCreate()

  val personDf = spark.read.options(Map(
    "header" -> "true",
    "ignoreLeadingWhiteSpace" -> "true",
    "ignoreTrailingWhiteSpace" -> "true",
    "inferSchema" -> "true" //,
    //    "mode" -> "FAILFAST"
    //    "mode" -> "DROPMALFORMED"
  )).csv("/Users/alexey/Downloads/wikipedia/output/person.csv")

  val nationality = personDf.col("nationality")
  val nationalityProjection = when(
    nationality
      .isin("American", "United States", "U.S.", "[[United States|American]]", "[[Americans|American]]", "[[United States]]", "USA", "{{USA}}"),
    "American")
    .otherwise(nationality)
    .as("nationality_new")

  personDf.groupBy(nationalityProjection).count().orderBy("count").show(truncate = false, numRows = 100)
}
