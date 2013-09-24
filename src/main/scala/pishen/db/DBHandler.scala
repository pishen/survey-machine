package pishen.db

import scala.collection.JavaConverters._

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.index.Index
import org.slf4j.LoggerFactory

class DBHandler(path: String) {
  private val logger = LoggerFactory.getLogger("DBHandler")

  logger.info("starting graphDB: " + path)

  private val graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(path)

  sys.ShutdownHookThread {
    logger.info("shutdown graphDB: " + path)
    graphDB.shutdown()
  }

  private val typeIndex: Index[Node] = graphDB.index().forNodes(DBHandler.TypeIndex)
  private val recordIndex: Index[Node] = graphDB.index().forNodes(DBHandler.RecordIndex)

  //need to go through the whole collection for it to close properly
  def records = {
    val nodes: java.util.Iterator[Node] = typeIndex.get(DBHandler.Type, DBHandler.Record)
    nodes.asScala.map(node => new Record(node)).toSeq
  }

  def getRecord(name: String) = {
    val node = recordIndex.get(Record.Name, name).getSingle()
    if (node != null) new Record(node) else null
  }

  def createRecord(name: String) {
    require(getRecord(name) == null, "create failed, already exists Record with name: " + name)

    val node = graphDB.createNode()
    node.setProperty(DBHandler.Type, DBHandler.Record)
    node.setProperty(Record.Name, name)
    typeIndex.add(node, DBHandler.Type, DBHandler.Record)
    recordIndex.add(node, Record.Name, name)
  }
}

object DBHandler {
  //key
  val Type = "TYPE"
  //value
  val Record = "RECORD"
  val Reference = "REFERENCE"
  //index
  val TypeIndex = "TYPE_INDEX"
  val RecordIndex = "RECORD_INDEX"
}