package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random
import pishen.db.CitationMark

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    /*val testCases = (1 to 50).par.map(i => {
      logger.info("test: " + i)
      TestCase(dbHandler.getRecord("journals-tog-ChenWC11"), 0.1, 50, 3, 0.05)
    })
    logger.info("cociationAP: " + testCases.map(_.cocitationAP).max)
    logger.info("katzAP: " + testCases.map(_.katzAP).max)*/

    val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == CitationMark.Type.Number &&
      r.outgoingRecords.filter(_.citationType == CitationMark.Type.Number).length >= 18
    }).map(r => {
      logger.info("create testcases")
      (1 to 10).map(i => TestCase(r, 0.5, 50, 3, 0.05))
    }).toSeq

    logger.info("# of Records: " + testCases.length)
    logger.info("C normal MAP: " + (testCases.map(_.head.cocitationAP).sum / testCases.length))
    logger.info("C best of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).max).sum / testCases.length))
    logger.info("C avg of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).sum / 10).sum / testCases.length))
    logger.info("K normal MAP: " + (testCases.map(_.head.katzAP).sum / testCases.length))
    logger.info("K best of 10 MAP: " + (testCases.map(_.map(_.katzAP).max).sum / testCases.length))
    logger.info("K avg of 10 MAP: " + (testCases.map(_.map(_.katzAP).sum / 10).sum / testCases.length))
  }
}