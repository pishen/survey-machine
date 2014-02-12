package core

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import collection.JavaConversions._

object AcmParser {
  import Main.logger

  def getRefSize(dblpKey: String) = {
    val acm = new File("dl-acm/" + dblpKey + ".html")
    Jsoup.parse(acm, "UTF-8", "http://dl.acm.org/")
      .getElementsByAttributeValue("name", "references")
      .first() match {
        case null => 0
        case a: Element => {
          a.parent()
            .nextElementSibling()
            .getElementsByTag("table")
            .first() match {
              case null => 0
              case table: Element => {
                table.getElementsByTag("tr").size()
              }
            }
        }
      }
  }

  def getLinks(dblpKey: String): Seq[(Int, String)] = {
    val acm = new File("dl-acm/" + dblpKey + ".html")
    Jsoup.parse(acm, "UTF-8", "http://dl.acm.org/")
      .getElementsByAttributeValue("name", "references")
      .first() match {
        case null => {
          logger.info("no references tag")
          Seq.empty
        }
        case a: Element => {
          a.parent()
            .nextElementSibling()
            .getElementsByTag("table")
            .first() match {
              case null => {
                logger.info("no table tag")
                Seq.empty
              }
              case table: Element => {
                table.getElementsByTag("tr")
                  .iterator()
                  .toSeq
                  .map(tr => {
                    tr.child(2)
                      .child(0)
                      .select("a")
                      .iterator()
                      .toSeq.reverse
                      .map(_.attr("href"))
                      .find(href => {
                        //TODO change to find from DB later
                        href.startsWith("http://doi.acm.org/")
                      })
                  })
                  .zipWithIndex
                  .map(p => (p._2 + 1, p._1))
                  .filter(_._2.nonEmpty)
                  .map(p => (p._1, p._2.get))
              }
            }
        }
      }
  }
}