package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import pishen.db.CitationMark
import pishen.db.Record
import java.io.File
import java.io.PrintWriter
import resource._

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

    /*dbHandler.records.filter(r => {
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
    })*/

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

    //degree statistic
    /*val result = dbHandler.records
      .map(_.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length).toSeq
      .groupBy(i => i).mapValues(_.length).toSeq.sortBy(_._1).reverse.map(p => p._1 + "," + p._2)
    val file = Resource.fromFile("degree-stat")
    file.truncate(0)
    file.writeStrings(result, "\n")*/

    //AP statistic
    /*val testCases = dbHandler.records
      .filter(_.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 12)
      .flatMap(r => {
        (1 to 5).map(i => TestCase(r, 0.3, 50, 3, 0.05))
      }).filter(_.cocitationRank.size == 50).toSeq

    writeTo("cocitation.csv"){out => 
      testCases.sortBy(_.cocitationAP).reverse.foreach(t => out.println(t.cocitationAP))
    }
    writeTo("new-cocitation.csv"){out =>
      testCases.sortBy(_.newCocitationAP).reverse.foreach(t => out.println(t.newCocitationAP))
    }*/

    //improvement stat
    /*val testCase = dbHandler.records.filter(r => {
      r.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 12
    }).flatMap(r => {
      (1 to 5).map(i => TestCase(r, 0.3, 50, 3, 0.05))
    }).filter(_.cocitationRank.size == 50).toSeq
    .sortBy(t => t.newCocitationAP - t.cocitationAP).reverse
    .foreach(t => logger.info((t.newCocitationAP - t.cocitationAP).toString))*/

    //best TestCase
    def dump(testCase: TestCase, filename: String) = {
      val f = (r: Record) => r != testCase.source &&
        r.year <= testCase.source.year &&
        r.citationType == Record.CitationType.Number

      managed(new PrintWriter(filename)).foreach(out => {
        out.println("source:")
        out.println(testCase.source.title)
        out.println("cocitation AP:")
        out.println(testCase.cocitationAP)
        out.println("new cocitation AP:")
        out.println(testCase.newCocitationAP)
        out.println("seeds:")
        testCase.seeds.foreach(r => out.println(r.title))
        out.println()
        testCase.answers.foreach(ans => {
          out.println("answer:")
          val cocitationIndex =
            if (testCase.cocitationRank.contains(ans)) testCase.cocitationRank.indexOf(ans) else 50
          val newCocitationIndex =
            if (testCase.newCocitationRank.contains(ans)) testCase.newCocitationRank.indexOf(ans) else 50
          val change = cocitationIndex - newCocitationIndex
          out.println(ans.title + "\t" + cocitationIndex + "->" + newCocitationIndex + "\t" + change)
          out.println("cocitings:")
          ans.incomingReferences.foreach(ansRef => {
            val cociting = ansRef.startRecord
            if (f(cociting)) {
              cociting.outgoingReferences.filter(_.endRecord match {
                case Some(end) => testCase.seeds.contains(end)
                case None      => false
              }).foreach(seedRef => {
                val minPair = ansRef.offsets
                  .flatMap(ansOff => seedRef.offsets.map(seedOff => Seq(ansOff, seedOff).sorted))
                  .minBy(seq => seq.last - seq.head)
                val section = cociting.fileContent match {
                  case Some(content) =>
                    content.substring(minPair.head - 20, minPair.last + 20).replaceAll("\n", " ")
                  case None => "file content error"
                }
                out.println(cociting.title + "\t" + ansRef.refIndex + "\t" + seedRef.refIndex + "\t" + section)
              })
            }
          })
        })
      })
    }
    
    val testCases = dbHandler.records.filter(r => {
      logger.info("check Record " + r.name)
      //r.citationType == Record.CitationType.Number &&
      r.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 12
    }).flatMap(r => {
      logger.info("create testcase")
      (1 to 5).map(i => TestCase(r, 0.3, 50, 3, 0.05))
    }).filter(_.cocitationRank.size == 50).toSeq

    val bestCase = testCases.maxBy(t => t.newCocitationAP - t.cocitationAP)
    val worstCase = testCases.minBy(t => t.newCocitationAP - t.cocitationAP)
    
    logger.info("dump best")
    dump(bestCase, "testcase-best.csv")
    logger.info("dump worst")
    dump(worstCase, "testcase-worst.csv")

    //list the details of a testcase
    /*val source = dbHandler.records.filter(r => {
      r.name.contains("sigir")
    }).maxBy(_.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length)
    
    logger.info("name: " + source.name + " title: " + source.title)
    val testCases = (1 to 10).map(i => TestCase(source, 0.3, 50, 3, 0.05))
    testCases.foreach(t => {
      logger.info("=====testcase=====")
      logger.info("seeds:")
      t.seeds.foreach(r => logger.info(r.name + "\t" + r.title))
      logger.info("answers:")
      t.answers.foreach(r => logger.info(r.name + "\t" + r.title))
      logger.info("cocitation:")
      t.cocitationRank.foreach(p => logger.info(p._1.name + "\t" + p._1.title))
      logger.info("new-cocitation:")
      t.newCocitationRank.foreach(r => logger.info(r.name + "\t" + r.title))
      logger.info("cocitation AP: " + t.cocitationAP)
      logger.info("new-cocitation AP: " + t.newCocitationAP)
    })*/

    //test on one source
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

    //main test
    /*val testCases = dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      //r.citationType == Record.CitationType.Number &&
      r.outgoingRecords.filter(_.citationType == Record.CitationType.Number).length >= 12
    }).map(r => {
      logger.info("create testcases")
      (1 to 10).map(i => TestCase(r, 0.3, 50, 3, 0.05))
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

    //write longest pair length
    /*dbHandler.records.filter(r => {
      logger.info("check record: " + r.name)
      r.citationType == Record.CitationType.Number &&
      r.outgoingReferences.length > 1
    }).foreach(r => {
      logger.info("write longest pair length")

      val length = r.outgoingReferences.combinations(2).map(s => {
        val lastOffsets = s.last.offsets
        s.head.offsets.flatMap(o1 => lastOffsets.map(o2 => (o1 - o2).abs)).min
      }).max

      val tx = dbHandler.beginTx
      try {
        r.writeLongestPairLength(if (length == 0) 1 else length)
        tx.success()
      } finally {
        tx.finish()
      }
    })*/

    def writeTo(filename: String)(op: PrintWriter => Unit) {
      val out = new PrintWriter(filename)
      try { op(out) } finally { out.close() }
    }

  }
}