package core

import java.io.File
import db.Neo4j
import db.Labels
import Main.logger
import db.Paper
import db.Neo4jOld
import pishen.db.Record
import org.neo4j.graphdb.Direction

object DbInitializer {
  def setupIndexes() = {
    Neo4j.createIndexIfNotExist(Labels.Paper, "dblpKey")
    Neo4j.createIndexIfNotExist(Labels.Paper, "ee")
    logger.info("wait for indexes")
    Neo4j.waitForIndexes(100)
  }

  def createPapersOld() = {
    new DblpIterator().foreach(p => {
      val nodeOpt = Neo4jOld.getRecord(p.dblpKey)
      if (nodeOpt.nonEmpty) {
        val node = nodeOpt.get
        val ty = Neo4jOld.getNodeProp(node, "CITATION_TYPE")
        val text = new File("text-records/" + p.dblpKey)
        if (ty == "NUMBER" && text.exists()) {
          Paper.createPaper(p.dblpKey, p.title, p.year.toString, p.ee)
        }
      }
    })
  }

  def connectPapersOld() = {
    Paper.allPapers.foreach(p => {
      val nodeOpt = Neo4jOld.getRecord(p.dblpKey)
      if (nodeOpt.nonEmpty) {
        logger.info("create Refs: " + p.dblpKey)
        val node = nodeOpt.get
        Neo4j.withTx {
          Neo4jOld.getRels(node, Record.Ref, Direction.OUTGOING)
            .map(Neo4jOld.getEndNode)
            .foreach(ref => {
              val index = Neo4jOld.getNodeProp(ref, "REF_INDEX").toInt
              val target = Neo4jOld.getRels(ref, Record.Ref, Direction.OUTGOING).map(Neo4jOld.getEndNode)
              if (target.nonEmpty) {
                val targetKey = Neo4jOld.getNodeProp(target.head, "NAME")
                Paper.getPaperByDblpKey(targetKey) match {
                  case Some(t) => p.createRefTo(t, index)
                  case None    => //should not happen
                }
              }
            })
        }
      }
    })
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