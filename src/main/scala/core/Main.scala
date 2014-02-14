package core

import org.slf4j.LoggerFactory

object Main {
  val logger = LoggerFactory.getLogger("main")

  def main(args: Array[String]): Unit = {
    if(args.contains("download")){
      Downloader.downloadCiteSeer()
    }
    if(args.contains("byYear")){
      Downloader.citeSeerByYear()
    }
    if(args.contains("blockify")){
      ContentParser.blockify()
    }
    if(args.contains("init")){
      //DbInitializer.setupIndexes
    }
  }
}