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
    //cut out content after REFERENCES
    //update Record length
    //match for [NUMBER with white space]
    //update offsets
    //update longest pair distance?
    
    val parsed = dbHandler.records.map(r => {
      r.fileContent match {
        case Some(c) => (true, c.lines.count(_.toLowerCase() == "references") > 0)
        case None => (false, false)
      }
    })
    
    logger.info("withContent: " + parsed.count(_._1))
    logger.info("with references: " + parsed.count(p => p._1 && p._2))
  }
}