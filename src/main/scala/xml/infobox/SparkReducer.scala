package xml.infobox

import java.io.{File, FileWriter}
import java.nio.file.Files

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}


object SparkReducer extends App {
  val infoBoxPropsMap = Map(
    "person" -> Person.properties,
    "settlement" -> Settlement.properties,
    "ort" -> Ort.properties,
    "writer" -> Writer.properties
  )

  // Parse input parameters
  val inputLocation = new File(args(0))
  val outputLocation = new File(args(1))

  val infoBoxProps =
    if (args.isDefinedAt(2) && infoBoxPropsMap.isDefinedAt(args(2)))
      infoBoxPropsMap(args(2))
    else
      sys.error(s"Invalid infoBox name: ${args(2)}, available: ${infoBoxPropsMap.keys}")

  if (!inputLocation.isDirectory) {
    sys.error("Input must be a directory: " + inputLocation.getAbsolutePath)
  }

  // Start Spark
  val conf = new SparkConf().setAppName("WikiInfoBoxReducer").setMaster("local[*]")
  val sc = new SparkContext(conf)

  val files = FileSystem.get(sc.hadoopConfiguration).listStatus(new Path(inputLocation.toString))
  Files.createDirectories(outputLocation.toPath)

  val sizeOfParallelChunk = 8
  val minimumPropertiesCount = 1

  // Process files
  val groupedFiles = files.grouped(sizeOfParallelChunk).toSeq
  groupedFiles.par.foreach(chunk => chunk.foreach { f =>
    val csv = sc.wholeTextFiles(f.getPath.toString)
      .map { case (path, content) =>
        val parsedProps = InfoBoxPropertiesParser.parse(content)
        if (parsedProps.size > minimumPropertiesCount) {
          val pageId = InfoBoxes.pathKey -> path.split("/").last.split("\\.").head
          val values = (infoBoxProps ++ parsedProps.filterKeys(k => infoBoxProps.contains(k)) + pageId).values
          Some(values.mkString(","))
        } else None
      }
      .collect()
      .flatten

    require(csv.length == 1, s"File ${f.getPath.toString} should have one Infobox")

    csv.foreach { content =>
      val writer = new FileWriter(s"$outputLocation/${f.getPath.getName}")
      writer.write(content)
      writer.close()
    }
  })

  sc.stop()
}


