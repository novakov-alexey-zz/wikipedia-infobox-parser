package xml.infobox

import org.scalatest.{FlatSpec, Matchers}

class InfoboxesTest extends FlatSpec with Matchers {

  it should "parse infoboxes properties from the config file" in {
    println(Person.properties.keys.mkString(","))
    Person.properties should contain allOf(
      "name" -> "",
      "native_name_lang" -> "",
      "known_for" -> "",
      "children" -> ""
    )
  }

}
