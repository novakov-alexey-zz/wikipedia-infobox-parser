package xml

import org.scalatest.{FlatSpec, Matchers}
import xml.infobox.{InfoboxPropertiesParser, Settlement}

import scala.io.Source

class SettlementPareserTest extends FlatSpec with Matchers {

  it should "parse 'settlement' infobox into map" in {
    //given
    val infobox = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("detroit_settlement.txt")).mkString
    //when
    val properties = InfoboxPropertiesParser.parse(infobox)
    //then
    properties should contain allOf(
      "name" -> "Detroit",
      "1" -> "[[Charles Pugh]] – Council President",
      "footnotes" -> "",
      "population_rank" -> "[[List of United States cities by population|18th in U.S.]]"
    )
  }
}
