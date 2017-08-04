package xml

import java.io.{ByteArrayInputStream, File}

import info.bliki.wiki.dump.{WikiPatternMatcher, WikiXMLParser}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.pull.{EvElemEnd, EvElemStart, EvText, XMLEventReader}

class PageParser(outputLocation: File = new File("output")) {

  def parseInfoBoxToCsv(inputXmlFileName: String) = {
    parse(inputXmlFileName, page => {
      page.infoBox.foreach(println)
    })
  }

  private def parse(inputXmlFileName: String, callback: Page => Unit) = {
    val xml = new XMLEventReader(Source.fromFile(inputXmlFileName))

    var insidePage = false
    var buf = ArrayBuffer[String]()
    for (event <- xml) {
      event match {
        case EvElemStart(_, "page", _, _) =>
          insidePage = true
          val tag = "<page>"
          buf += tag
        case EvElemEnd(_, "page") =>
          val tag = "</page>"
          buf += tag
          insidePage = false

          parsePage(buf.mkString).foreach(callback)
          buf.clear
        case e@EvElemStart(_, tag, _, _) =>
          if (insidePage) {
            buf += ("<" + tag + ">")
          }
        case e@EvElemEnd(_, tag) =>
          if (insidePage) {
            buf += ("</" + tag + ">")
          }
        case EvText(t) =>
          if (insidePage) {
            buf += t
          }
        case _ => // ignore
      }
    }
  }


  def parsePage(text: String): Option[Page] = {
    val wrappedPage = new WrappedPage
    //The parser occasionally throws exceptions out, we ignore these
    try {
      val parser = new WikiXMLParser(new ByteArrayInputStream(text.getBytes), new SetterArticleFilter(wrappedPage))
      parser.parse()
    } catch {
      case e: Exception =>
    }

    val page = wrappedPage.page
    val infoBox = Option(new WikiPatternMatcher(text).getInfoBox).map(_.dumpRaw())
    if (page.getText != null && page.getTitle != null && page.getId != null
      && page.getRevisionId != null && page.getTimeStamp != null
      && !page.isCategory && !page.isTemplate) {
      Some(Page(page.getTitle, page.getText, page.isFile, infoBox))
    } else {
      None
    }
  }

  //
  //  def writePage(buf: ArrayBuffer[String]) = {
  //    val s = buf.mkString
  //    val x = XML.loadString(s)
  //    val pageId = (x \ "id").head.child.head.toString
  //    val f = new File(outputLocation, pageId + ".xml")
  //    println("writing to: " + f.getAbsolutePath)
  //    val out = new FileOutputStream(f)
  //    try {
  //      out.write(s.getBytes())
  //    } catch {
  //      case NonFatal(throwable) => sys.error(s"error '${throwable.getMessage}' while saving page ${f.getAbsolutePath}")
  //    } finally out.close()
  //  }
}
