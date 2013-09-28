package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.outgoingRecords.length >= 50
    }).map(r => {
      logger.info("create testcase")
      TestCase(r, 0.1, 150)
    }).toSeq
    
    //logger.info("APs: " + testCases.map(_.cocitationAP).mkString(", "))
    //logger.info("length: " + testCases.length)
    logger.info("MAP: " + (testCases.map(_.cocitationAP).sum / testCases.length))
    
  }
}