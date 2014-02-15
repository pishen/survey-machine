package core

import org.slf4j.LoggerFactory
import db.Neo4jOld
import java.io.File
import scala.xml.XML

object Main {
  val logger = LoggerFactory.getLogger("main")

  def main(args: Array[String]): Unit = {
    val oneYear = new File("citeseer-raw").listFiles().map(f => {
      (XML.loadFile(f) \\ "record").count(n => {
        (n \\ "date").lastOption match {
          case None => false
          case Some(d) => d.text.substring(0, 4).toInt == 2010
        }
      })
    }).sum
    println("2010: " + oneYear)
  }
}