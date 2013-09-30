package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    /*val testCases: Seq[TestCase] = for(i <- 1 to 10) yield{
      logger.info("test: " + i)
      TestCase(dbHandler.getRecord("journals-sigir-Aoe90a"), 0.5, 200)
    }*/
    //logger.info("source: " + testCase.source.name)
    //logger.info("rank")
    //testCase.cocitationRank.foreach(logger info _._1.name)
    //logger.info("answer")
    //testCase.answers.foreach(logger info _.name)
    //logger.info("seeds")
    //testCase.seeds.foreach(logger info _.name)
    //logger.info("AP: " + testCase.cocitationAP)

    val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.outgoingRecords.length >= 18
    }).map(r => {
      logger.info("create testcases")
      for (i <- 1 to 10) yield TestCase(r, 0.1, 100)
    }).toSeq

    //logger.info("APs: " + testCases.map(_.cocitationAP).mkString(", "))
    //logger.info("length: " + testCases.length)
    logger.info("normal MAP: " + (testCases.map(_.head.cocitationAP).sum / testCases.length))
    logger.info("best of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).max).sum / testCases.length))

  }
}