package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import scala.util.Random

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    val testCases = dbHandler.records.filter(_.outgoingRecords.length >= 100)
      .map(r => TestCase(r, 0.1, 50))
    
    logger.info("MAP: " + (testCases.map(_.cocitationAP).sum / testCases.length))
    
  }
}