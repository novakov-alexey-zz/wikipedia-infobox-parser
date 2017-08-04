package xml

import info.bliki.wiki.dump.{IArticleFilter, Siteinfo, WikiArticle}
import org.xml.sax.SAXException


class SetterArticleFilter(val wrappedPage: WrappedPage) extends IArticleFilter {
  @throws(classOf[SAXException])
  def process(page: WikiArticle, siteinfo: Siteinfo) {
    wrappedPage.page = page
  }
}

/**
  * A helper class that allows for a WikiArticle to be serialized and also pulled from the XML parser
  *
  * @param page The WikiArticle that is being wrapped
  */
case class WrappedPage(var page: WikiArticle = new WikiArticle) {}

/**
  * Represents a parsed Wikipedia page from the Wikipedia XML dump
  *
  * https://en.wikipedia.org/wiki/Wikipedia:Database_download
  * https://meta.wikimedia.org/wiki/Data_dump_torrents#enwiki
  *
  * @param title      Title of the current page
  * @param text       Text of the current page including markup
  * @param isFile     Is the page a file page, not perfectly accurate
  */
case class Page(title: String, text: String, isFile: Boolean, infoBox: Option[String])

