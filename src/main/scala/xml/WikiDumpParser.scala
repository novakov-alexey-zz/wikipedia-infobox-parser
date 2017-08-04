package xml

import java.io.File


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

  new PageParser().parseInfoBoxToCsv(inputXmlFile)
}
