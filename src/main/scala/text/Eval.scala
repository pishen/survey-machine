package text

object Eval {
  def computeAP(ranks: Seq[Paper], answers: Seq[Paper]) = {
    require(answers.size > 0)
    val precisions = ranks.zipWithIndex.filter(answers contains _._1)
      .zipWithIndex.map(p => (p._2 + 1) / (p._1._2 + 1).toDouble)
    precisions.sum / answers.size
  }

  def computeF1(ranks: Seq[Paper], answers: Seq[Paper]) = {
    val hits = ranks.intersect(answers)
    val precision = hits.size / ranks.size.toDouble
    val recall = hits.size / answers.size.toDouble
    2 * precision * recall / (precision + recall)
  }
}