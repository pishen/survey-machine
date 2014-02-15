package pdf

import java.io.File

import scala.sys.process.stringSeqToProcess
import scala.sys.process.stringToProcess
import scala.xml.Elem
import scala.xml.XML

import com.rockymadden.stringmetric.similarity.LevenshteinMetric

import main.Main.logger

object Downloader {
  private var ports: Seq[Int] = 10001 to 10010
  def switchPort() {
    val head = ports.head
    ports = ports.tail :+ head
  }

  def curl(url: String, output: String, timeout: Int = 300) = {
    Seq(
      "curl",
      "-L",
      "-k",
      "-g",
      "--connect-timeout", "10",
      "-m", timeout.toString,
      "-o", output,
      "-A", "Mozilla/5.0",
      "--socks5", "localhost:" + ports.head,
      url).!
  }

}