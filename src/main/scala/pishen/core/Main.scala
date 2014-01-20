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
    parseDBLPAndDownload()
  }

  def parseDBLPAndDownload() = {
    "mkdir google-scholar".!
    "mkdir dl-acm".!
    "mkdir paper-pdf".!

    val ports = 10001 to 10010
    var port = ports.head

    def downloadScholar(key: String, title: String) = {
      val file = new File("google-scholar/" + key + ".html")
      if (!file.exists() || Resource.fromFile(file).string.contains("302 Moved")) {
        logger.info("downloadScholar")
        val res = curl("http://scholar.google.com.tw/scholar?q=" + title, file.getPath(), port)
        assert(!Resource.fromFile(file).string.contains("302 Moved") && res == 0)
        true
      } else {
        false
      }
    }

    def downloadACM(key: String, ee: String) = {
      val file = new File("dl-acm/" + key + ".html")
      if (!file.exists()) {
        val doid = ee.split("/").last
        val url = "http://dl.acm.org/citation.cfm?doid=" + doid + "&preflayout=flat"
        logger.info("downloadACM: " + url)
        assert(curl(url, file.getPath(), port) == 0)
        true
      } else {
        false
      }
    }

    def downloadPDF(key: String) = {
      val pdfFile = new File("paper-pdf/" + key + ".pdf")
      if (!pdfFile.exists()) {
        val scholarFile = new File("google-scholar/" + key + ".html")
        Jsoup.parse(scholarFile, "UTF-8", "http://scholar.google.com.tw/")
          .select("div.gs_r")
          .first()
          .select("a").iterator()
          .map(_.attr("href"))
          .find(_.endsWith(".pdf")) match {
            case Some(url) => {
              logger.info("downloadPDF: " + url)
              curl(url, pdfFile.getPath(), port)
              //assert(res == 0 || res == 6 || res == 9)
              true
            }
            case None => false
          }
      } else {
        false
      }
    }

    val papers = new PaperIterator
    papers.filter(p => {
      (p \ "year").text.toInt >= 2010 &&
        (p \ "ee").text.startsWith("http://doi.acm.org")
    }).foreach(p => {
      val key = (p \ "@key").text.replaceAll("/", "-")
      logger.info("paper: " + key)

      val title = (p \ "title").text.replaceAll(" ", "+")
      val ee = (p \ "ee").text

      val res1 = downloadScholar(key, title)
      val res2 = downloadACM(key, ee)
      val res3 = downloadPDF(key)

      if (res1 || res2 || res3) {
        port += 1
        if (port > ports.last) port = ports.head
        Thread.sleep(10000)
      }
    })
  }

  def curl(url: String, output: String, port: Int) = {
    val ver = Random.shuffle(24 to 26).head.toString + ".0"
    Seq(
      "curl",
      "-L",
      "-k",
      "--connect-timeout", "10",
      "-o", output,
      "-A", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:" + ver + ") Gecko/20100101 Firefox/" + ver,
      "--socks5", "localhost:" + port,
      url).!
  }

}