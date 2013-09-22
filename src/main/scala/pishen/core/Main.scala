package pishen.core

import org.slf4j.LoggerFactory

import pishen.db.DBHandler

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("/media/pishen/DATA/new-graph-db")

    dbHandler.records.take(10).foreach(record => {
      logger.info("Parsing Record: " + record.name)
      record.allReferences.foreach(reference => {
        logger.info("Reference link length: " + reference.links.length)
      })
    })

  }
}