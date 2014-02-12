package core

import org.slf4j.LoggerFactory
import scala.xml.XML
import scala.xml.NodeSeq
import scalax.io.Resource
import java.io.FileWriter
import db.Neo4j
import sys.process._

object Main {
  val logger = LoggerFactory.getLogger("main")

  def main(args: Array[String]): Unit = {
    if(args.contains("download")){
      Downloader.download()
    }
    if(args.contains("blockify")){
      ContentParser.blockify()
    }
    if(args.contains("init")){
      //DbInitializer.setupIndexes
    }
  }

}