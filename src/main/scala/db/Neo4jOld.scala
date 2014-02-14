package db

import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.Label
import collection.JavaConversions._
import org.neo4j.graphdb.DynamicLabel
import org.neo4j.graphdb.DynamicRelationshipType
import core.Main.logger
import java.util.concurrent.TimeUnit
import org.neo4j.tooling.GlobalGraphOperations
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.factory.GraphDatabaseSettings

object Neo4jOld {
  val graphDb = new GraphDatabaseFactory()
    .newEmbeddedDatabaseBuilder("graph-db-old")
    .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
    .newGraphDatabase()

  sys.ShutdownHookThread {
    logger.info("shutdown old graphDb")
    graphDb.shutdown()
  }

  //Nodes

  val recordIndex = graphDb.index().forNodes("RECORD_INDEX")

  def getRecord(name: String) = withTx {
    val node = recordIndex.get("NAME", name).getSingle()
    if (node != null) Some(node.getId()) else None
  }

  def getNodeProp(nodeId: Long, key: String) = withTx {
    graphDb.getNodeById(nodeId).getProperty(key).asInstanceOf[String]
  }

  def getNodePropOption(nodeId: Long, key: String) = withTx {
    val res = graphDb.getNodeById(nodeId).getProperty(key, null).asInstanceOf[String]
    if (res == null) None else Some(res)
  }

  //Relationships

  def getRels(nodeId: Long, relType: RelationshipType, direction: Direction) = withTx {
    graphDb.getNodeById(nodeId).getRelationships(relType, direction).map(_.getId()).toSeq
  }

  def getStartNode(relId: Long) = withTx {
    graphDb.getRelationshipById(relId).getStartNode().getId()
  }

  def getEndNode(relId: Long) = withTx {
    graphDb.getRelationshipById(relId).getEndNode().getId()
  }

  //Transaction

  def withTx[R](operations: => R) = {
    val tx = graphDb.beginTx()
    try {
      val res = operations
      tx.success()
      res
    } finally {
      tx.close()
    }
  }
}