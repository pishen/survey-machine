package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random
import pishen.db.CitationMark

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    val testCases = Range(1, 10).par.map(i => {
      logger.info("test: " + i)
      TestCase(dbHandler.getRecord("journals-sigir-Aoe90a"), 0.1, 50, 3, 0.05)
    })
    logger.info("cociationAP: " + testCases.map(_.cocitationAP).max)
    logger.info("katzAP: " + testCases.map(_.katzAP).max)

    /*val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == CitationMark.Type.Number &&
      r.outgoingRecords.filter(_.citationType == CitationMark.Type.Number).length >= 18
    }).map(r => {
      logger.info("create testcases")
      for (i <- 1 to 10) yield TestCase(r, 0.1, 50, 3, 0.05)
    }).toSeq

    logger.info("# of Records: " + testCases.length)
    logger.info("C normal MAP: " + (testCases.map(_.head.cocitationAP).sum / testCases.length))
    logger.info("C best of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).max).sum / testCases.length))
    logger.info("K normal MAP: " + (testCases.map(_.head.katzAP).sum / testCases.length))
    logger.info("K best of 10 MAP: " + (testCases.map(_.map(_.katzAP).max).sum / testCases.length))*/
  }
}