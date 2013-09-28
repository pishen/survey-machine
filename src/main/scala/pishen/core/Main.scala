package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    val testCase = TestCase(dbHandler.records.take(1).toSeq.head, 0.1, 50)
    logger.info("source: " + testCase.source.name)
    logger.info("rank")
    testCase.cocitationRank.foreach(logger info _._1.name)
    logger.info("answer")
    testCase.answers.foreach(logger info _.name)
    logger.info("seeds")
    testCase.seeds.foreach(logger info _.name)
    logger.info("AP: " + testCase.cocitationAP)
    
    /*val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.outgoingRecords.length >= 50
    }).map(r => {
      logger.info("create testcase")
      TestCase(r, 0.1, 150)
    }).toSeq*/
    
    //logger.info("APs: " + testCases.map(_.cocitationAP).mkString(", "))
    //logger.info("length: " + testCases.length)
    //logger.info("MAP: " + (testCases.map(_.cocitationAP).sum / testCases.length))
    
  }
}