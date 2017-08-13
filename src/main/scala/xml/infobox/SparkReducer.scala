package xml.infobox

import java.io.{File, FileWriter}
import java.nio.file.Files

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}


object SparkReducer extends App {
  val inputLocation = new File(args(0))
  val outputLocation = new File(args(1))
  val infoboxProps = if (args.isDefinedAt(2) && args(2) == "person") Person.properties else Settlement.properties

  if (!inputLocation.isDirectory) {
    val msg = "Input must be a directory: " + inputLocation.getAbsolutePath
    throw new Exception(msg)
  }

  val conf = new SparkConf().setAppName("WikiInfoboxReducer").setMaster("local[*]")
  val sc = new SparkContext(conf)

  val files = FileSystem.get(sc.hadoopConfiguration).listStatus(new Path(inputLocation.toString))
  Files.createDirectories(outputLocation.toPath)

  val sizeOfParallelChunk = 8

  files.grouped(sizeOfParallelChunk).toSeq.par.foreach(chunk => chunk.foreach { f =>
    val csv = sc.wholeTextFiles(f.getPath.toString)
      .map { case (path, content) => path -> InfoboxPropertiesParser.parse(content) }
      .map { case (path, parsedProps) =>
        if (parsedProps.size > 1) {
          val pageId = path.split("/").last.split("\\.").head
          val values = (infoboxProps ++ parsedProps.filterKeys(k => infoboxProps.contains(k)) + (Infoboxes.pathKey -> pageId)).values
          val csvRow = values.map(v => if (v.contains(",")) s""""$v"""" else v).mkString(",")
          Some(csvRow)
        } else None
      }.collect()

    require(csv.length == 1, s"File ${f.getPath.toString} should have exactly one Infobox, but was ${csv.length}")

    csv.head.foreach { content =>
      val writer = new FileWriter(s"$outputLocation/${f.getPath.getName}")
      writer.write(content)
      writer.close()
    }
  })

  sc.stop()
}


