package xml

import java.io.{ByteArrayInputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}

import info.bliki.wiki.dump.{WikiPatternMatcher, WikiXMLParser}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.control.NonFatal
import scala.xml.XML
import scala.xml.pull.{EvElemEnd, EvElemStart, EvText, XMLEventReader}

class PageParser(outputLocation: File = new File("output")) {

  def parseInfoBoxToCsv(inputXmlFileName: String, infoboxFilter: Set[String]) = {
    parseXml(inputXmlFileName, page => {
      infoboxFilter.foreach { infobox =>
        if (page.infoBox.startsWith("{{Infobox " + infobox)) {
          writePage(infobox, page.pageId, ArrayBuffer(page.infoBox))
        }
      }
    })
  }

  private def parseXml(inputXmlFileName: String, callback: Page => Unit) = {
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

          parsePageInfobox(buf.mkString).foreach(callback)
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


  private def parsePageInfobox(text: String): Option[Page] = {
    val infoBox = Option(new WikiPatternMatcher(text).getInfoBox).map(_.dumpRaw())

    if (infoBox.isEmpty)
      return None

    val wrappedPage = new WrappedPage
    //The parser occasionally throws exceptions out, we ignore these
    try {
      val parser = new WikiXMLParser(new ByteArrayInputStream(text.getBytes), new SetterArticleFilter(wrappedPage))
      parser.parse()
    } catch {
      case e: Exception =>
    }

    val page = wrappedPage.page
    lazy val pageId = {
      val textElem = XML.loadString(text)
      (textElem \ "id").head.child.head.toString
    }

    if (page.getText != null && page.getTitle != null && page.getId != null
      && page.getRevisionId != null && page.getTimeStamp != null
      && !page.isCategory && !page.isTemplate && infoBox.isDefined) {
      Some(Page(pageId, page.getTitle, page.getText, page.isFile, infoBox.get))
    } else {
      None
    }
  }


  private def writePage(infoboxName: String, pageId: String, buf: ArrayBuffer[String]) = {
    val s = buf.mkString
    val path = Paths.get(outputLocation.toString, infoboxName)
    Files.createDirectories(path)
    val fullPath = path.resolve(pageId + ".txt").toAbsolutePath.toFile

    println("writing to: " + fullPath)
    val out = new FileOutputStream(fullPath)

    try {
      out.write(s.getBytes())
    } catch {
      case NonFatal(throwable) => sys.error(s"error '${throwable.getMessage}' while saving page $fullPath")
    } finally out.close()
  }
}
