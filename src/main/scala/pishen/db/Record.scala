package pishen.db

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.slf4j.LoggerFactory
import scala.io.Source
import resource._
import Record.CitationType
import java.io.FileNotFoundException

class Record(node: Node) {
  private val logger = LoggerFactory.getLogger("Record")

  def nodeId = node.getId()
  def fileContent = try {
    managed(Source.fromFile("text-records/" + name)).map(_.mkString).opt
  } catch {
    case ex: FileNotFoundException => None
  }

  //properties
  def name = getStringProperty(Record.Name)
  def ee = getStringProperty(Record.EE)
  def title = getStringProperty(Record.Title)
  def year = getStringProperty(Record.Year).toInt
  def emb = getStringProperty(Record.Emb)
  def refFetched = getStringProperty(Record.RefFetched)
  def citationType = getStringProperty(CitationType.toString)
  def length = node.getProperty(Record.Length).asInstanceOf[Int]
  def longestPairLength = node.getProperty(Record.LongestPairLength).asInstanceOf[Int]
  private def getStringProperty(key: String) = node.getProperty(key).asInstanceOf[String]

  def writeCitationType(cType: String) = node.setProperty(CitationType.toString, cType)
  def writeLength(length: Int) = node.setProperty(Record.Length, length)
  def writeLongestPairLength(length: Int) = node.setProperty(Record.LongestPairLength, length)

  //relationships
  def allNeighborRecords = (outgoingRecords ++ incomingRecords).distinct
  def outgoingRecords = outgoingReferences.map(_.endRecord).filter(_ != None).map(_.get).distinct
  def incomingRecords = incomingReferences.map(_.startRecord).distinct

  def allReferences = outgoingReferences ++ incomingReferences
  def outgoingReferences =
    getRelationships(Direction.OUTGOING, Record.Ref).map(rel => new Reference(rel.getEndNode()))
  def incomingReferences =
    getRelationships(Direction.INCOMING, Record.Ref).map(rel => new Reference(rel.getStartNode()))

  private def getRelationships(direction: Direction, relType: RelationshipType) =
    node.getRelationships(direction, relType).asScala.iterator.toSeq

  //equality
  override def equals(other: Any) =
    other match {
      case that: Record => (that canEqual this) && nodeId == that.nodeId
      case _            => false
    }
  def canEqual(other: Any) = other.isInstanceOf[Record]
  override def hashCode = nodeId.hashCode

}

object Record {
  //key
  val Name = "NAME"
  val EE = "EE"
  val Title = "TITLE"
  val Year = "YEAR"
  val Emb = "EMB"
  val RefFetched = "REF_FETCHED"
  val Length = "LENGTH"
  val LongestPairLength = "LongestPairLength"
  object CitationType {
    override def toString = "CITATION_TYPE"
    val Number = "NUMBER"
    val Text = "TEXT"
    val Unknown = "UNKNOWN"
  }
  //relationship
  val Ref = DynamicRelationshipType.withName("REF")
}