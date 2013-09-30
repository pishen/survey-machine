package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random
import pishen.db.CitationMark

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    val testCases: Seq[TestCase] = for(i <- 1 to 10) yield{
      logger.info("test: " + i)
      TestCase(dbHandler.getRecord("journals-sigir-Aoe90a"), 0.1, 50, 4, 0.05)
    }
    logger.info("cociationAP: " + testCases.map(_.cocitationAP).max)
    logger.info("katzAP: " + testCases.map(_.katzAP).max)

    /*val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == CitationMark.Type.Number &&
      r.outgoingRecords.filter(_.citationType == CitationMark.Type.Number).length >= 18
    }).map(r => {
      logger.info("create testcases")
      for (i <- 1 to 10) yield TestCase(r, 0.1, 50, 3, 0.05)
    }).toSeq*/

    //logger.info("APs: " + testCases.map(_.cocitationAP).mkString(", "))
    //logger.info("# of Records: " + testCases.length)
    //logger.info("normal MAP: " + (testCases.map(_.head.cocitationAP).sum / testCases.length))
    //logger.info("best of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).max).sum / testCases.length))

  }
}