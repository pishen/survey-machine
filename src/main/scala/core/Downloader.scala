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
    "mkdir citeseer-raw".!

    def downloadRecords(year: Int, index: Int, token: String): Unit = {
      val file = new File("citeseer-raw/" + year + "/" + index + ".xml")
      if (!file.exists()) {
        if (token == null) {
          val base = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&from=" +
            year +
            "-01-01&until=" +
            year +
            "-12-31&metadataPrefix=oai_dc"
          logger.info("curl: " + base)
          assert(curl(base, file.getPath()) == 0)
        } else {
          val resume = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&resumptionToken=" + token
          logger.info("curl: " + resume)
          assert(curl(resume, file.getPath()) == 0)
        }
      }
      val xml = XML.loadFile(file)
      (xml \\ "resumptionToken").headOption match {
        case None    => Unit
        case Some(n) => downloadRecords(year, index + 1, n.text)
      }
    }
    (2010 to 2013).foreach(year => {
      Seq("mkdir", "-p", "citeseer-raw/" + year).!
      downloadRecords(year, 0, null)
    })
  }

  def extractCiteSeer() = {
    /*def findRecord() = {
      new File("citeseer").listFiles().find(f => {
        val xml = XML.loadFile(f)
        xml \\ "record"
      })
    }*/

    new DblpIterator().foreach(p => {
      val cleanedTitle = p.title.toLowerCase()

    })
  }

  def download() = {
    "mkdir dl-acm".!
    "mkdir paper-pdf".!

    val ports = 10001 to 10010
    var port = ports.head

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

    def downloadPDF(p: DblpPaper) = {
      val pdfFile = new File("paper-pdf/" + p.dblpKey + ".pdf")
      if (!pdfFile.exists()) {
        false
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

      val res1 = downloadACM(p.dblpKey, p.ee)
      val res2 = downloadPDF(p)

      if (res1 || res2) {
        port += 1
        if (port > ports.last) port = ports.head
        Thread.sleep(7000)
      }
    })
  }

  def curl(url: String, output: String) = {
    Seq(
      "curl",
      "-k",
      "-o", output,
      "-A", "Mozilla/5.0",
      url).!
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

}