package core

import java.io.File
import scala.collection.JavaConversions.asScalaIterator
import scala.sys.process.stringSeqToProcess
import scala.sys.process.stringToProcess
import scala.util.Random
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scalax.io.Resource
import scala.xml.XML

object Downloader {
  import Main.logger
  def downloadCiteSeer() = {
    "mkdir citeseer".!
    val base = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&from=2010-01-01&until=2014-12-31&metadataPrefix=oai_dc"
    def downloadRecords(index: Int, token: String): Unit = {
      val file = new File("citeseer/" + index + ".xml")
      if (!file.exists()) {
        logger.info("download citeseer: " + index)
        if (token == null) {
          assert(curl(base, file.getPath(), 10001) == 0)
        } else {
          val resume = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&resumptionToken=" + token
          assert(curl(resume, file.getPath(), 10001) == 0)
        }
      }
      val xml = XML.loadFile(file)
      (xml \\ "resumptionToken").headOption match {
        case None    => Unit
        case Some(n) => downloadRecords(index + 1, n.text)
      }
    }
    downloadRecords(0, null)
  }

  def download() = {
    //"mkdir google-scholar".!
    "mkdir dl-acm".!
    "mkdir paper-pdf".!

    val ports = 10001 to 10010
    var port = ports.head

    def downloadScholar(key: String, title: String) = {
      val file = new File("google-scholar/" + key + ".html")
      lazy val content = Resource.fromFile(file).string
      if (!file.exists() ||
        content.contains("302 Moved") ||
        content.contains("請在上方的搜尋框中輸入查詢字詞") ||
        content == "") {
        logger.info("downloadScholar")
        val res = curl("http://scholar.google.com.tw/scholar?q=" + title, file.getPath(), port)
        val newContent = Resource.fromFile(file).string
        assert(!newContent.contains("302 Moved") && newContent != "" && res == 0)
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
        if (!Resource.fromFile(scholarFile).string.contains("沒有任何文章符合您的搜尋")) {
          Jsoup.parse(scholarFile, "UTF-8", "http://scholar.google.com.tw/")
            .select("div.gs_r")
            .first()
            .select("a").iterator()
            .map(_.attr("href"))
            .find(_.endsWith(".pdf")) match {
              case Some(url) => {
                logger.info("downloadPDF: " + url)
                val res = curl(url, pdfFile.getPath(), port)
                //assert(res == 0 || res == 6 || res == 9)
                res == 0
              }
              case None => false
            }
        } else {
          false
        }
      } else {
        false
      }
    }

    val papers = new DblpIterator
    papers.filter(p => {
      p.year >= 2010 &&
        p.ee.startsWith("http://doi.acm.org")
    }).foreach(p => {
      logger.info("paper: " + p.dblpKey)

      val title = p.title.replaceAll(" ", "+").replaceAll("#", "")

      val res1 = downloadScholar(p.dblpKey, title)
      val res2 = downloadACM(p.dblpKey, p.ee)
      val res3 = downloadPDF(p.dblpKey)

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
      "-g",
      "--connect-timeout", "10",
      "-m", "300",
      "-o", output,
      "-A", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:" + ver + ") Gecko/20100101 Firefox/" + ver,
      "--socks5", "localhost:" + port,
      url).!
  }

  def checkTitleEditDistance() = {
    new DblpIterator().foreach(p => {
      val scholar = new File("google-scholar/" + p.dblpKey + ".html")
      if (scholar.exists()) {
        Jsoup.parse(scholar, "UTF-8", "http://scholar.google.com.tw/")

      }
    })
  }
}