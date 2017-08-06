package xml

import java.io.File
import java.nio.file.{Files, Paths}

import scala.io.Source
import scala.util.Try


object WikiDumpParser extends App {

  if (args.isEmpty) {
    sys.error("specify input file name")
    System.exit(0)
  }

  val inputXmlFile = args(0)
  val outputLocation = new File(args(1))

  if (!outputLocation.exists()) {
    println("Creating output directory: " + outputLocation.getAbsolutePath)
    outputLocation.mkdirs()
  }

  if (!outputLocation.isDirectory) {
    val msg = "Output must be a directory: " + outputLocation.getAbsolutePath
    throw new Exception(msg)
  }

  val lastSeenPageId = if (Files.exists(Paths.get("lastSeenPageId.txt"))) {
    Source.fromFile("lastSeenPageId.txt").getLines().toList.headOption.map(_.trim).filter(s => Try(s.toLong).isSuccess)
  } else None

  lastSeenPageId.foreach(l => println(s"Going to use lastSeenPageId: $l"))
  PageParser().parseInfoBoxToCsv(inputXmlFile, Set("person", "settlement"), lastSeenPageId)
}
