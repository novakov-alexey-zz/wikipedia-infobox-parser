package xml.infobox

import java.io.{File, FileWriter}
import java.nio.file.Files

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}


object SparkReducer extends App {

  val inputLocation = new File(args(0))
  val outputLocation = new File(args(1))

  if (!inputLocation.isDirectory) {
    val msg = "Input must be a directory: " + inputLocation.getAbsolutePath
    throw new Exception(msg)
  }

  val conf = new SparkConf().setAppName("WikiInfoboxReducer").setMaster("local[*]")
  val sc = new SparkContext(conf)

  val files = FileSystem.get(sc.hadoopConfiguration).listStatus(new Path(inputLocation.toString))

  Files.createDirectories(outputLocation.toPath)

  val infoboxProps = if (args.isDefinedAt(2) && args(2) == "2") Person.properties else Settlement.properties

  files.grouped(8).toSeq.par.foreach(group => group.foreach { f =>
    val tsv = sc.wholeTextFiles(f.getPath.toString)
      .map { case (path, content) => path -> InfoboxPropertiesParser.parse(content) }
      .map { case (path, parsedProps) =>
        if (parsedProps.size > 1) {
          val pageId = path.split("/").last.split("\\.").head
          Some((infoboxProps ++ parsedProps + (Infoboxes.pathKey -> pageId)).values.mkString("\t"))
        } else None
      }.collect()

    tsv.head.foreach { content =>
      val writer = new FileWriter(s"$outputLocation/${f.getPath.getName}")
      writer.write(content)
      writer.close()
    }
  })
  sc.stop()
}


