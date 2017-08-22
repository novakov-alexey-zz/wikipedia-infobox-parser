package xml.dump

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime

import scala.io.Source
import scala.util.Try


object WikiDumpParser extends App {

  if (args.isEmpty) {
    sys.error("specify input file name")
    System.exit(0)
  }

  // Input parameters
  val inputXmlFile = args(0)
  val outputLocation = new File(args(1))
  val infoboxNames =
    if (args.isDefinedAt(2)) args(2).split(",").map(_.trim)
    else throw new Exception("Specify 3rd arg with comma separated infobox names")
  val outDirPrefix =
    if (args.isDefinedAt(3)) args(3).trim
    else LocalDateTime.now().toString


  createDir(outputLocation)
  infoboxNames.map(new File(_)).foreach(createDir)

  if (!outputLocation.isDirectory) {
    val msg = "Output must be a directory: " + outputLocation.getAbsolutePath
    throw new Exception(msg)
  }

  val lastSeenPageId = if (Files.exists(Paths.get("lastSeenPageId.txt"))) {
    Source.fromFile("lastSeenPageId.txt").getLines().toList.headOption.map(_.trim).filter(s => Try(s.toLong).isSuccess)
  } else None

  lastSeenPageId.foreach(l => println(s"Going to use lastSeenPageId: $l"))
  PageParser().parseInfoBoxToCsv(inputXmlFile, infoboxNames.toSet, outDirPrefix, lastSeenPageId)

  private def createDir(dir: File) = {
    if (!dir.exists()) {
      println("Creating output directory: " + dir.getAbsolutePath)
      dir.mkdirs()
    }
  }
}
