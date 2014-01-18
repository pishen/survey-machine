package pishen.core

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.sys.process.stringSeqToProcess
import scala.sys.process.stringToProcess
import scala.util.Random

import org.slf4j.LoggerFactory

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    "mkdir google-scholar".!

    val papers = new PaperIterator
    papers.filter(p => {
      (p \ "year").text.toInt >= 2010 &&
        (p \ "ee").text.startsWith("http://doi.acm.org")
    }).foreach(p => {
      val key = (p \ "@key").text.replaceAll("/", "-")
      val title = (p \ "title").text.replaceAll(" ", "+")
      logger.info("download scholar info: " + key)
      val res = download("http://scholar.google.com.tw/scholar?q=" + title,
        "google-scholar/" + key + ".html")
      assert(res == 0)
      Thread.sleep(2000 + Random.nextInt(3000))
    })
  }

  def download(url: String, output: String) = {
    //"http://scholar.google.com.tw/scholar?q=" + title
    Seq(
      "curl",
      "-o", output,
      "-A", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:26.0) Gecko/20100101 Firefox/26.0",
      "--socks5", "localhost:9999",
      url).!
  }

}