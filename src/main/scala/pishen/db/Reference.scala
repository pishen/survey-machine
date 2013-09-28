package pishen.db

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import org.slf4j.LoggerFactory
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.RelationshipType

class Reference(node: Node) {
  private val logger = LoggerFactory.getLogger("Reference")

  def nodeId = node.getId()
  
  //properties
  def refIndex = getStringProperty(Reference.RefIndex).toInt
  def content = getStringProperty(Reference.Content)
  def links = node.getProperty(Reference.Links).asInstanceOf[Array[String]]
  private def getStringProperty(key: String) = node.getProperty(key).asInstanceOf[String]

  //relationships
  def startRecord = 
    getRelationships(Direction.INCOMING, Reference.Ref).map(rel => new Record(rel.getStartNode())).head
  def hasEndRecord = !getRelationships(Direction.OUTGOING, Reference.Ref).isEmpty
  def endRecord =
    getRelationships(Direction.OUTGOING, Reference.Ref).map(rel => new Record(rel.getEndNode())).head
  private def getRelationships(direction: Direction, relType: RelationshipType) =
    node.getRelationships(direction, relType).asScala.iterator.toSeq
}

object Reference {
  //key
  val RefIndex = "REF_INDEX"
  val Content = "CONTENT"
  val Links = "LINKS"
  //relationship
  val Ref = DynamicRelationshipType.withName("REF")
}