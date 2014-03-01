package text

import java.io.File
import org.neo4j.graphdb.Direction
import main.Main.logger
import pishen.db.Record
import scalax.io.Resource
import java.io.FileWriter

object DbInitializer {
  def setupIndexes() = {
    Neo4j.createIndexIfNotExist(Labels.Paper, "dblpKey")
    Neo4j.createIndexIfNotExist(Labels.Paper, "ee")
    logger.info("wait for indexes")
    Neo4j.waitForIndexes(100)
  }

  def createPapers() = {
    logger.info("creating papers")
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
    logger.info("renew dblp-keys")
    val dblpKeys = new DblpIterator().flatMap(p => {
      println("check " + p.dblpKey)
      Paper.getPaperByDblpKey(p.dblpKey).map(_.dblpKey)
    })
    Resource.fromWriter(new FileWriter("dblp-keys")).writeStrings(dblpKeys.toSeq, "\n")
  }

  def connectPapers() = {
    Paper.allPapers.foreach(p => {
      val nodeOpt = Neo4jOld.getRecord(p.dblpKey)
      assert(nodeOpt.nonEmpty)
      logger.info("create refs: " + p.dblpKey)
      val node = nodeOpt.get
      Neo4j.withTx {
        Neo4jOld.getRels(node, Record.Ref, Direction.OUTGOING)
          .map(Neo4jOld.getEndNode)
          .foreach(ref => {
            val index = Neo4jOld.getNodeProp(ref, "REF_INDEX").toInt
            val target = Neo4jOld.getRels(ref, Record.Ref, Direction.OUTGOING).map(Neo4jOld.getEndNode)
            if (target.nonEmpty) {
              val targetKey = Neo4jOld.getNodeProp(target.head, "NAME")
              val targetOpt = Paper.getPaperByDblpKey(targetKey)
              assert(targetOpt.nonEmpty)
              p.createRefTo(targetOpt.get, index)
            }
          })
      }
    })
  }

}