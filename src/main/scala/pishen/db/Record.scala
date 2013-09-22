package pishen.db

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.slf4j.LoggerFactory

class Record(node: Node) {
  private val logger = LoggerFactory.getLogger("Record")

  def name = getStringProperty(Record.Name)
  def ee = getStringProperty(Record.EE)
  def title = getStringProperty(Record.Title)
  def year = getStringProperty(Record.Year).toInt
  def emb = getStringProperty(Record.Emb)
  def refFetched = getStringProperty(Record.RefFetched)
  def citationType = getStringProperty(Record.CitationType)

  def allNeighborRecords = outgoingRecords ++ incomingRecords
  def outgoingRecords = outgoingReferences.filter(_.hasEndRecord).map(_.endRecord)
  def incomingRecords = incomingReferences.map(_.startRecord)

  def allReferences = outgoingReferences ++ incomingReferences
  def outgoingReferences =
    getRelationships(Direction.OUTGOING, Record.Ref).map(rel => new Reference(rel.getEndNode()))
  def incomingReferences =
    getRelationships(Direction.INCOMING, Record.Ref).map(rel => new Reference(rel.getStartNode()))

  private def getRelationships(direction: Direction, relType: RelationshipType) =
    node.getRelationships(direction, relType).asScala.view
  private def getStringProperty(key: String) = node.getProperty(key).asInstanceOf[String]
}

object Record {
  //key
  val Name = "NAME"
  val EE = "EE"
  val Title = "TITLE"
  val Year = "YEAR"
  val Emb = "EMB"
  val RefFetched = "REF_FETCHED"
  val CitationType = "CITATION_TYPE"
  //relationship
  val Ref = DynamicRelationshipType.withName("REF")
}