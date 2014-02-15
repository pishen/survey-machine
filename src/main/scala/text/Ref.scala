package text

case class Ref(id: Long) {
  lazy val index = Neo4j.getRelProp(id, "index")
  lazy val startPaper = Paper(Neo4j.getStartNode(id))
  lazy val endPaper = Paper(Neo4j.getEndNode(id))
}