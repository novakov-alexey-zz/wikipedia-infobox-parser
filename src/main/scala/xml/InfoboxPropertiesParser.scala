package xml

object InfoboxPropertiesParser {

  def parse(text: String): Map[String, String] = {
    val lines = text.split("\n").drop(1).dropRight(1)
    lines.map(l => {
      val properties = l.split('=')
      val key = properties(0).dropWhile(c => c == ' ' || c == '|').trim
      val value = if (properties.isDefinedAt(1)) properties(1).trim else ""
      key -> value
    }).toMap
  }
}