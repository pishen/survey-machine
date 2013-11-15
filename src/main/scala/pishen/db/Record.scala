package pishen.db

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import org.slf4j.LoggerFactory
import scala.io.Source
import Record.CitationType
import java.io.FileNotFoundException
import scalax.io.Resource

class Record(node: Node) {
  private val logger = LoggerFactory.getLogger("Record")

  lazy val nodeId = node.getId()
  lazy val fileContent = try {
    Some(Resource.fromFile("text-records/" + name).string)
  } catch {
    case ex: FileNotFoundException => None
  }

  //properties
  lazy val name = getStringProperty(Record.Name)
  lazy val ee = getStringProperty(Record.EE)
  lazy val title = getStringProperty(Record.Title)
  lazy val year = getStringProperty(Record.Year).toInt
  lazy val emb = getStringProperty(Record.Emb)
  lazy val refFetched = getStringProperty(Record.RefFetched)
  lazy val citationType = getStringProperty(CitationType.toString)
  lazy val length = node.getProperty(Record.Length).asInstanceOf[Int]
  lazy val longestPairLength = node.getProperty(Record.LongestPairLength).asInstanceOf[Int]
  private def getStringProperty(key: String) = node.getProperty(key).asInstanceOf[String]

  def writeCitationType(cType: String) = node.setProperty(CitationType.toString, cType)
  def writeLength(length: Int) = node.setProperty(Record.Length, length)
  def writeLongestPairLength(length: Int) = node.setProperty(Record.LongestPairLength, length)

  //relationships
  lazy val allNeighborRecords = (outgoingRecords ++ incomingRecords).distinct
  lazy val outgoingRecords = outgoingReferences.map(_.endRecord).filter(_ != None).map(_.get).distinct
  lazy val incomingRecords = incomingReferences.map(_.startRecord).distinct

  lazy val allReferences = outgoingReferences ++ incomingReferences
  lazy val outgoingReferences =
    getRelationships(Direction.OUTGOING, Record.Ref).map(rel => new Reference(rel.getEndNode()))
  lazy val incomingReferences =
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