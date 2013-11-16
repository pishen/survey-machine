package pishen.core

import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import pishen.db.CitationMark
import pishen.db.Record
import java.io.File
import java.io.PrintWriter

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")

    //TODO
    //test how many paper has only one "REFERENCES\n"
    //cut out content after REFERENCES -- fail
    //update Record length
    //match for [NUMBER with white space]
    //update offsets
    //update longest pair distance?

    dbHandler.records.foreach(r => {
      logger.info("check r: " + r.name)
      val tx = dbHandler.beginTx
      try {
        ContentParser.writeContentInfo(r)
        tx.success()
      } finally {
        tx.finish()
      }

    })

  }
}