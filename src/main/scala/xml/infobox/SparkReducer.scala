package xml.infobox

import java.io.{File, FileWriter}
import java.nio.file.Files

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}


object SparkReducer extends App {
  val infoboxPropsMap = Map(
    "person" -> Person.properties,
    "settlement" -> Settlement.properties,
    "ort" -> Ort.properties,
    "writer" -> Writer.properties
  )

  // Parse input parameters
  val inputLocation = new File(args(0))
  val outputLocation = new File(args(1))

  val infoboxProps =
    if (args.isDefinedAt(2) && infoboxPropsMap.isDefinedAt(args(2)))
      infoboxPropsMap(args(2))
    else
      throw new Exception(s"Invalid infobox name: ${args(2)}, available: ${infoboxPropsMap.keys}")

  if (!inputLocation.isDirectory) {
    throw new Exception("Input must be a directory: " + inputLocation.getAbsolutePath)
  }

  // Start Spark
  val conf = new SparkConf().setAppName("WikiInfoboxReducer").setMaster("local[*]")
  val sc = new SparkContext(conf)

  val files = FileSystem.get(sc.hadoopConfiguration).listStatus(new Path(inputLocation.toString))
  Files.createDirectories(outputLocation.toPath)

  val sizeOfParallelChunk = 8

  // Process files
  val groupedFiles = files.grouped(sizeOfParallelChunk).toSeq
  groupedFiles.par.foreach(chunk => chunk.foreach { f =>
    val csv = sc.wholeTextFiles(f.getPath.toString)
      .map { case (path, content) => path -> InfoboxPropertiesParser.parse(content) }
      .map { case (path, parsedProps) =>
        if (parsedProps.size > 1) {
          val pageId = path.split("/").last.split("\\.").head
          val values = (infoboxProps ++ parsedProps.filterKeys(k => infoboxProps.contains(k)) + (Infoboxes.pathKey -> pageId)).values
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


