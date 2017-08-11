package xml.infobox

import java.nio.file.Paths

import scala.collection.mutable
import scala.io.Source

object Infoboxes {
  val pathKey = "path"
}

trait Infoboxes {
  def getKeys(fileName: String): Seq[String] = {
    Infoboxes.pathKey +:
    Source.fromFile(Paths.get("config", fileName).toFile).getLines()
      .filterNot(l => l.startsWith("<!--") || l.startsWith("{{") || l.startsWith("}}")) //skip comments, header, footer
      .map(_.split("=").headOption.map(_.trim.dropWhile(c => c == ' ' || c == '|'))) // take only keys
      .flatten
      .to[Seq]
  }

  val keys: Seq[String]

  def properties: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap(keys.map(e => e -> ""): _*)
}

object Settlement extends Infoboxes {
  override val keys: Seq[String] = getKeys("settlement-props.txt")
}

object Person extends Infoboxes {
  override val keys: Seq[String] = getKeys("person-props.txt")
}