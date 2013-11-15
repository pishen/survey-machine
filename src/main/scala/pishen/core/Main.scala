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
    
    val refRegex = "[^a-zA-Z]*references?[^a-zA-Z]*".r
    /*val parsed = dbHandler.records.map(r => {
      r.fileContent match {
        case Some(c) => (true, c.lines.count(s => refRegex.findFirstIn(s.toLowerCase()).isEmpty == false) > 0)
        case None => (false, false)
      }
    }).toSeq
    
    logger.info("withContent: " + parsed.count(_._1))
    logger.info("with references: " + parsed.count(p => p._1 && p._2))*/
    dbHandler.records.flatMap(r => {
      r.fileContent match {
        case Some(c) => {
          val matches = c.lines.filter(s => refRegex.pattern.matcher(s).matches())
          matches.drop(matches.length - 1)
        }
        case None => Seq.empty
      }
    }).toSeq.distinct.foreach(s => logger.info(s))
  }
}