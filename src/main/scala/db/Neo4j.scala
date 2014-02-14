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

object Neo4j {
  val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("graph-db-old")

  sys.ShutdownHookThread {
    logger.info("shutdown graphDb")
    graphDb.shutdown()
  }

  //Nodes
  
  def createNode(label: Label, kv: Seq[(String, String)]) = withTx {
    val node = graphDb.createNode(label)
    kv.foreach { case (key, value) => node.setProperty(key, value) }
    node.getId()
  }

  def getNodes(label: Label, key: String, value: String) = withTx {
    graphDb.findNodesByLabelAndProperty(label, key, value).map(_.getId()).toSeq
  }

  def getNodeProp(nodeId: Long, key: String) = withTx {
    graphDb.getNodeById(nodeId).getProperty(key).asInstanceOf[String]
  }

  def getNodePropOption(nodeId: Long, key: String) = withTx {
    val res = graphDb.getNodeById(nodeId).getProperty(key, null).asInstanceOf[String]
    if (res == null) None else Some(res)
  }

  def setNodeProps(nodeId: Long, kv: Seq[(String, String)]) = withTx {
    val node = graphDb.getNodeById(nodeId)
    kv.foreach { case (key, value) => node.setProperty(key, value) }
  }
  
  //Relationships
  
  def createRel(relType: RelationshipType, from: Long, to: Long, kv: Seq[(String, String)]) = withTx {
    val fromNode = graphDb.getNodeById(from)
    val toNode = graphDb.getNodeById(to)
    val rel = fromNode.createRelationshipTo(toNode, relType)
    kv.foreach { case (key, value) => rel.setProperty(key, value) }
    rel.getId()
  }
  
  def getRels(nodeId: Long, relType: RelationshipType, direction: Direction) = withTx {
    graphDb.getNodeById(nodeId).getRelationships(relType, direction).map(_.getId()).toSeq
  }
  
  def getRelProp(relId: Long, key: String) = withTx {
    graphDb.getRelationshipById(relId).getProperty(key).asInstanceOf[String]
  }
  
  def getRelPropOption(relId: Long, key: String) = withTx {
    val res = graphDb.getRelationshipById(relId).getProperty(key, null).asInstanceOf[String]
    if(res == null) None else Some(res)
  }
  
  def setRelProps(relId: Long, kv: Seq[(String, String)]) = withTx {
    val rel = graphDb.getRelationshipById(relId)
    kv.foreach { case (key, value) => rel.setProperty(key, value) }
  }
  
  def getStartNode(relId: Long) = withTx {
    graphDb.getRelationshipById(relId).getStartNode().getId()
  }
  
  def getEndNode(relId: Long) = withTx {
    graphDb.getRelationshipById(relId).getEndNode().getId()
  }

  //Indexes
  
  def createIndexIfNotExist(label: Label, key: String) = withTx {
    if (!graphDb.schema().getIndexes(label).exists(_.getPropertyKeys().exists(_ == key))) {
      graphDb.schema().indexFor(label).on(key).create()
    }
  }

  def waitForIndexes(seconds: Long) = {
    graphDb.schema().awaitIndexesOnline(seconds, TimeUnit.SECONDS)
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

object Labels {
  val Paper = DynamicLabel.label("Paper")
}

object Relationships {
  val Ref = DynamicRelationshipType.withName("Ref")
}