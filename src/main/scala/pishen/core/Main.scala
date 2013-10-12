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

    dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == Record.CitationType.Number
    }).foreach(r => {
      if (ContentParser.detectType(r) == Record.CitationType.Unknown) {
        logger.info("fix type")
        val tx = dbHandler.beginTx()
        try {
          r.writeCitationType(Record.CitationType.Unknown)
          r.outgoingReferences.foreach(_.eraseOffsets())
          tx.success()
        } finally {
          tx.finish()
        }
      }
    })

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

    /*val testCases = dbHandler.records.find(r => {
      r.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 30
    }) match {
      case Some(r) => (1 to 10).map(i => {
        logger.info("test: " + i)
        TestCase(r, 0.1, 50, 3, 0.05)
      })
      case None => throw new Exception("should not happen")
    }
    logger.info("C MAP of 10: " + (testCases.map(_.cocitationAP).sum / 10))
    logger.info("C best AP of 10: " + (testCases.map(_.cocitationAP).max))
    logger.info("NC MAP of 10: " + (testCases.map(_.newCocitationAP).sum / 10))
    logger.info("NC best AP of 10: " + (testCases.map(_.newCocitationAP).max))*/

    /*val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      //r.citationType == Record.CitationType.Number &&
      r.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 12
    }).map(r => {
      logger.info("create testcases")
      (1 to 10).map(i => TestCase(r, 0.1, 50, 3, 0.05))
    }).toSeq

    logger.info("# of Records: " + testCases.length)
    //logger.info("C normal MAP: " + (testCases.map(_.head.cocitationAP).sum / testCases.length))
    logger.info("C best of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).max).sum / testCases.length))
    logger.info("C avg of 10 MAP: " + (testCases.map(_.map(_.cocitationAP).sum / 10).sum / testCases.length))
    //logger.info("K normal MAP: " + (testCases.map(_.head.katzAP).sum / testCases.length))
    //logger.info("K best of 10 MAP: " + (testCases.map(_.map(_.katzAP).max).sum / testCases.length))
    //logger.info("K avg of 10 MAP: " + (testCases.map(_.map(_.katzAP).sum / 10).sum / testCases.length))
    logger.info("NC best of 10 MAP: " + (testCases.map(_.map(_.newCocitationAP).max).sum / testCases.length))
    logger.info("NC avg of 10 MAP: " + (testCases.map(_.map(_.newCocitationAP).sum / 10).sum / testCases.length))*/

    //citations

    //write all citation offsets for Number Records

    /*dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == Record.CitationType.Number
    }).foreach(r => {
      logger.info("write offsets")
      val tx = dbHandler.beginTx()
      try {
        ContentParser.writeOffsetsForAllRef(r)
        tx.success()
      } finally {
        tx.finish()
      }

    })*/

    //write all article's length

    /*dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == Record.CitationType.Number
    }).foreach(r => {
      logger.info("write length")
      val tx = dbHandler.beginTx
      try {
        r.fileContent match {
          case Some(c) => r.writeLength(c.length())
          case None => throw new Exception("Number Record must have content")
        }
        tx.success()
      } finally {
        tx.finish()
      }
    })*/

  }
}