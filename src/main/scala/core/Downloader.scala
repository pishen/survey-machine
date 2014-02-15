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
import java.io.FileWriter
import com.rockymadden.stringmetric.similarity.LevenshteinMetric
import scala.xml.Elem

object Downloader {
  import Main.logger

  def downloadCiteSeer() = {
    "mkdir citeseer-raw".!
    def downloadRecords(index: Int, token: String): Unit = {
      val file = new File("citeseer-raw/" + index + ".xml")
      if (!file.exists()) {
        if (token == null) {
          val base = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&metadataPrefix=oai_dc"
          logger.info("curl: " + base)
          assert(curl(base, file.getPath()) == 0)
        } else {
          val resume = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&resumptionToken=" + token
          logger.info("curl: " + resume)
          assert(curl(resume, file.getPath()) == 0)
        }
      }
      logger.info("check " + file.getName())
      val xml = XML.loadFile(file)
      (xml \\ "resumptionToken").headOption match {
        case None    => Unit
        case Some(n) => downloadRecords(index + 1, n.text)
      }
    }
    downloadRecords(0, null)
  }

  def citeSeerByYear() = {
    val records = new File("citeseer-raw").listFiles().flatMap(f => {
      (XML.loadFile(f) \\ "record").filter(n => {
        val dateStr = (n \\ "date").lastOption
        if (dateStr.nonEmpty) {
          dateStr.get.text.substring(0, 4).toInt == 2010
        } else {
          false
        }
      })
    })
    Resource.fromWriter(new FileWriter("citeseer-year/2010.xml"))
      .write(<root>{ records }</root>.toString)
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

    def downloadPDF(p: DblpPaper, xml: Elem): Boolean = {
      val pdfFile = new File("paper-pdf/" + p.dblpKey + ".pdf")
      val dblpTitle = p.title.toLowerCase()
      if (!pdfFile.exists()) {
        (xml \\ "record").find(n => {
          val citeSeerTitle = (n \\ "title").head.text.toLowerCase()
          LevenshteinMetric.compare(dblpTitle, citeSeerTitle) match {
            case None    => false
            case Some(i) => i < 6
          }
        }) match {
          case None => false
          case Some(n) => (n \\ "source").headOption match {
            case None => false
            case Some(s) => {
              val url = s.text
              curl(url, pdfFile.getPath()) == 0
            }
          }
        }
      } else {
        false
      }
    }

    val papers = new DblpIterator
    papers.filter(p => {
      p.year == 2010 && p.ee.startsWith("http://doi.acm.org")
    }).foreach(p => {
      logger.info("paper: " + p.dblpKey)

      val res1 = downloadACM(p.dblpKey, p.ee)
      val xml = XML.loadFile("citeseer-year/" + p.year + ".xml")
      downloadPDF(p, xml)

      if (res1) {
        port += 1
        if (port > ports.last) port = ports.head
        Thread.sleep(10000)
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