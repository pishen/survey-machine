package text

import scala.Option.option2Iterable
import org.neo4j.graphdb.Direction
import main.Main.logger
import scalax.io.Resource

case class Paper(id: Long) {
  def dblpKey = Neo4j.getNodeProp(id, "dblpKey")
  def title = Neo4j.getNodeProp(id, "title")
  def year = Neo4j.getNodeProp(id, "year").toInt
  def ee = Neo4j.getNodeProp(id, "ee")
  def content = Resource.fromFile("text-records/" + dblpKey).string

  def outgoingRefs = Neo4j.getRels(id, Relationships.Ref, Direction.OUTGOING).map(Ref(_)).toSet
  def incomingRefs = Neo4j.getRels(id, Relationships.Ref, Direction.INCOMING).map(Ref(_)).toSet

  def outgoingPapers = outgoingRefs.map(_.endPaper)
  def incomingPapers = incomingRefs.map(_.startPaper)

  def createRefTo(target: Paper, index: Int) {
    if (!outgoingPapers.contains(target)) {
      Neo4j.createRel(Relationships.Ref, id, target.id, Seq("index" -> index.toString))
    }
  }
}

object Paper {
  def createPaper(dblpKey: String, title: String, year: String, ee: String) {
    val sameDblp = getPaperByDblpKey(dblpKey)
    val sameEe = getPaperByEe(ee)
    //make sure this paper's dblpKey and ee are unique 
    if (sameDblp.isEmpty && sameEe.isEmpty) {
      logger.info("create paper: " + dblpKey)
      Neo4j.createNode(Labels.Paper, Seq(
        "dblpKey" -> dblpKey,
        "title" -> title,
        "year" -> year,
        "ee" -> ee))
    } else {
      logger.info("paper " + dblpKey + " already exist")
    }
  }

  def getPaperByDblpKey(dblpKey: String) = {
    val papers = Neo4j.getNodes(Labels.Paper, "dblpKey", dblpKey).map(Paper(_))
    assert(papers.size <= 1) //check unique
    papers.headOption
  }

  def getPaperByEe(ee: String) = {
    val papers = Neo4j.getNodes(Labels.Paper, "ee", ee).map(Paper(_))
    assert(papers.size <= 1) //check unique
    papers.headOption
  }

  def allPapers() = {
    Resource.fromFile("dblp-keys").lines().flatMap(getPaperByDblpKey _)
  }
}