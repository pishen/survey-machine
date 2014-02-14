package core

import org.slf4j.LoggerFactory
import db.Neo4jOld

object Main {
  val logger = LoggerFactory.getLogger("main")

  def main(args: Array[String]): Unit = {
    DbInitializer.setupIndexes()
  }
}