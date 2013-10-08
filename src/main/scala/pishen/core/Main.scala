package pishen.core

import org.slf4j.LoggerFactory
import scalax.io.Resource
import pishen.db.DBHandler
import pishen.db.CitationMark
import pishen.db.Record

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    //content parsing

    /*println(dbHandler.records.count(r => {
      println("checking " + r.name)
      ContentParser.detectType(r) match {
        case Some(t) => t == CitationMark.Type.Number
        case None    => false
      }
    }))*/

    /*val tx = dbHandler.beginTx()
    try {
      dbHandler.records.foreach(r => {
        println("checking " + r.name)
        r.writeCitationType(ContentParser.detectType(r))
      })
      tx.success()
    } finally {
      tx.finish()
    }*/

    /*dbHandler.records.find(r => ContentParser.detectType(r) == Record.CitationType.Number) match {
      case Some(r) => {
        println("record: " + r.name)
        ContentParser.findAllCitations(r) match {
          case Some(iter) => iter.foreach(println _)
          case None       => println("No citations")
        }
      }
      case None => println("None")
    }*/

    /*dbHandler.records.find(r => r.citationType == CitationMark.Type.Number) match {
      case Some(r) => println(r.name)
      case None => println("None")
    }*/

    /*ContentParser.findAllCitations(dbHandler.getRecord("journals-tog-ChenWC11")) match {
      case Some(iter) => iter.foreach(p => logger.info(p._1 + "\t" + p._2))
      case None => logger.info("None")
    }*/

    //graph structure testing

    val result = dbHandler.records
      .map(_.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length).toSeq
      .groupBy(i => i).mapValues(_.length).toSeq.sortBy(_._1).reverse.map(p => p._1 + "," + p._2)
    Resource.fromFile("degree-stat").writeStrings(result, "\n")

    /*dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == Record.CitationType.Number
    }).map(_.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length).toSeq
      .groupBy(i => i).mapValues(_.length).toSeq.sortBy(_._1).reverse.foreach(p => println(p._1 + "," + p._2))*/

    /*val testCases = (1 to 50).par.map(i => {
      logger.info("test: " + i)
      TestCase(dbHandler.getRecord("journals-tog-ChenWC11"), 0.1, 50, 3, 0.05)
    })
    logger.info("cociationAP: " + testCases.map(_.cocitationAP).max)
    logger.info("katzAP: " + testCases.map(_.katzAP).max)*/

    /*val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == Record.CitationType.Number &&
      r.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 10
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
    logger.info("K avg of 10 MAP: " + (testCases.map(_.map(_.katzAP).sum / 10).sum / testCases.length))*/
  }
}