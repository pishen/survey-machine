package pishen.db

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

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

  def records = {
    val nodes: java.lang.Iterable[Node] = typeIndex.get(DBHandler.Type, DBHandler.Record)
    nodes.asScala.view.map(node => new Record(node))
  }

  def getRecord(name: String) = {
    val nodes: java.lang.Iterable[Node] = recordIndex.get(Record.Name, name)
    val records = nodes.asScala.map(node => new Record(node))
    if (records.isEmpty) {
      throw new NoSuchElementException("no Record with name: " + name)
    } else {
      records.head
    }
  }

  def createRecord(name: String) {
    val nodes: java.lang.Iterable[Node] = recordIndex.get(Record.Name, name)
    require(nodes.asScala.isEmpty, "already exists Record with name: " + name)

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