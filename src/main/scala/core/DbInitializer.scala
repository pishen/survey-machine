package core

import java.io.File
import db.Neo4j
import db.Labels
import Main.logger
import db.Paper

object DbInitializer {
  def setupIndexes() = {
    Neo4j.createIndexIfNotExist(Labels.Paper, "dblpKey")
    Neo4j.createIndexIfNotExist(Labels.Paper, "ee")
    logger.info("wait for indexes")
    Neo4j.waitForIndexes(100)
  }

  def createPapers() = {
    new DblpIterator().foreach(p => {
      val blocksFile = new File("paper-pdf-blockify/" + p.dblpKey + "_spatial.xml")
      if (blocksFile.exists() &&
        ContentParser.isNumericIndex(p.dblpKey)) {
        Paper.createPaper(p.dblpKey, p.title, p.year.toString, p.ee)
      }
    })
  }

  def connectPapers() = {
    Paper.allPapers.foreach(p => {
      logger.info("create Refs: " + p.dblpKey)
      AcmParser.getLinks(p.dblpKey).foreach {
        case (index, ee) =>
          Paper.getPaperByEe(ee) match {
            case None              => //do nothing
            case Some(targetPaper) => p.createRefTo(targetPaper, index)
          }
      }
    })
  }
}