package pishen.db

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import org.slf4j.LoggerFactory
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.RelationshipType

class Reference(node: Node) {
  private val logger = LoggerFactory.getLogger("Reference")

  lazy val nodeId = node.getId()

  //properties
  lazy val refIndex = getStringProperty(Reference.RefIndex).toInt
  lazy val content = getStringProperty(Reference.Content)
  lazy val links = node.getProperty(Reference.Links).asInstanceOf[Array[String]]
  lazy val offsets = node.getProperty(Reference.Offsets).asInstanceOf[Array[Int]]
  private def getStringProperty(key: String) = node.getProperty(key).asInstanceOf[String]

  def writeOffsets(offsets: Seq[Int]) = node.setProperty(Reference.Offsets, offsets.toArray)
  def eraseOffsets() = node.removeProperty(Reference.Offsets)

  //relationships
  lazy val startRecord =
    getRelationships(Direction.INCOMING, Reference.Ref).map(rel => new Record(rel.getStartNode())).head
  lazy val endRecord = {
    val rels = getRelationships(Direction.OUTGOING, Reference.Ref)
    if (rels.isEmpty) None
    else Some(rels.map(rel => new Record(rel.getEndNode())).head)
  }
  private def getRelationships(direction: Direction, relType: RelationshipType) =
    node.getRelationships(direction, relType).asScala.iterator.toSeq
}

object Reference {
  //key
  val RefIndex = "REF_INDEX"
  val Content = "CONTENT"
  val Links = "LINKS" //Array[String]
  val Offsets = "OFFSETS" //Array[Int]
  //relationship
  val Ref = DynamicRelationshipType.withName("REF")
}