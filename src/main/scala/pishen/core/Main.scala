package pishen.core

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.sys.process.stringSeqToProcess
import scala.sys.process.stringToProcess
import scala.util.Random
import org.slf4j.LoggerFactory
import scalax.io.Resource
import java.io.File

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
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
        
        port = Random.shuffle(ports.filter(_ != port)).head
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