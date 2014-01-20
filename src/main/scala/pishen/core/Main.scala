package pishen.core

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.sys.process.stringSeqToProcess
import scala.sys.process.stringToProcess
import scala.util.Random
import org.slf4j.LoggerFactory
import scalax.io.Resource
import java.io.File
import scala.xml.XML
import org.jsoup.Jsoup
import collection.JavaConversions._

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val scholar = new File("google-scholar/conf-afrigraph-Skala10.html")
    val doc = Jsoup.parse(scholar, "UTF-8", "http://scholar.google.com.tw/")
    val str = doc.select("div.gs_r").first().select("a").iterator()
      .filter(_.attr("href").endsWith(".pdf"))
      .map(_.attr("href"))
      .mkString("\n")
    /*val cleaner = new HtmlCleaner
    val node = cleaner.clean(Resource.fromFile(scholar).string)
    val serializer = new SimpleHtmlSerializer(cleaner.getProperties())
    val str = serializer.getAsString(node)*/
    /*val raw = Resource.fromFile(scholar).string
    val start = raw.indexOf("<body>")
    val end = raw.indexOf("</body>") + 7
    val str = raw.substring(start, end).replaceAll("&*;", "")*/
    println(str)
    //val xml = XML.loadString(str)
    /*val count = scholar.listFiles().count(scholar => {
      val xml = XML.loadString(Resource.fromFile(scholar).string)
      xml.\\("div").find(_.\("@class").text == "gs_r") match {
        case Some(div) => div.\\("a").find(_.\("@href").text.endsWith(".pdf")).nonEmpty
        case None => false
      }
    })
    println(count)*/
  }
  
  def parseDBLP() = {
    "mkdir google-scholar".!

    val ports = 10001 to 10010
    var port = ports.head
    val papers = new PaperIterator
    papers.filter(p => {
      (p \ "year").text.toInt >= 2010 &&
        (p \ "ee").text.startsWith("http://doi.acm.org")
    }).foreach(p => {
      val key = (p \ "@key").text.replaceAll("/", "-")
      val title = (p \ "title").text.replaceAll(" ", "+")
      logger.info("download scholar info: " + key)

      val filename = "google-scholar/" + key + ".html"
      val file = new File(filename)

      if (!file.exists() || Resource.fromFile(file).string.contains("302 Moved")) {
        download("http://scholar.google.com.tw/scholar?q=" + title, filename, port)

        assert(!Resource.fromFile(file).string.contains("302 Moved"))
        
        port += 1
        if(port > ports.last) port = ports.head
        
        Thread.sleep(10000)
      }
    })
  }

  def download(url: String, output: String, port: Int) = {
    //"http://scholar.google.com.tw/scholar?q=" + title
    val ver = Random.shuffle(20 to 26).head.toString + ".0"
    Seq(
      "curl",
      "-o", output,
      "-A", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:" + ver + ") Gecko/20100101 Firefox/" + ver,
      "--socks5", "localhost:" + port,
      url).!
  }

}