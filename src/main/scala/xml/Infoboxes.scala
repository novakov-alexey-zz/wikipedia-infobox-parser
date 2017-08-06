package xml

import java.nio.file.Paths

import scala.collection.mutable
import scala.io.Source

trait Infoboxes {
  def getKeys(fileName: String): Seq[String] = {
    Source.fromFile(Paths.get("config", fileName).toFile).getLines()
      .filter(!_.startsWith("<!--"))
      .map(_.split("=").headOption).flatten.to[Seq]
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