package xml.infobox

import scala.collection.mutable

object InfoboxPropertiesParser {
  val commentPrefix = "!--"

  /**
    *
    * @param text the Infobox body itself
    * @return Map of names and their values
    */
  def parse(text: String): mutable.LinkedHashMap[String, String] = {
    val lines = text.split("\n").drop(1).dropRight(1)
    mutable.LinkedHashMap(
      lines
        .filterNot(l => l.trim.length == 0 || l.startsWith(commentPrefix))
        .map(l => {
          val property = l.split('=')
          val key = property(0).dropWhile(c => c == ' ' || c == '|').trim
          val value = if (property.isDefinedAt(1)) property(1).trim else ""
          val cleanedValue = if (value.startsWith(commentPrefix)) "" else value
          val escapedValue = if (cleanedValue.contains(",")) s""""$cleanedValue"""" else cleanedValue
          key -> escapedValue
        }): _*)
  }
}