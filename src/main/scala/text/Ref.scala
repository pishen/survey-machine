package text

case class Ref(id: Long) {
  def index = Neo4j.getRelProp(id, "index").toInt
  def startPaper = Paper(Neo4j.getStartNode(id))
  def endPaper = Paper(Neo4j.getEndNode(id))
}