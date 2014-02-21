package text

import math._

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
    if (hits.size == 0) 0.0 else 2 * precision * recall / (precision + recall)
  }

  def computeNDCG(ranks: Seq[Paper], answers: Seq[Paper]) = {
    val dcg = ranks.zipWithIndex.map { case (p, i) => (p, i + 1) }
      .map { case (p, i) => (if (answers.contains(p)) 1 else 0) / log(1 + i) }.sum
    val idcg = (1 to ranks.intersect(answers).size).map(i => 1 / log(1 + i)).sum
    dcg / idcg
  }

  def computeRR(ranks: Seq[Paper], answers: Seq[Paper]) = {
    val rank = ranks.indexWhere(answers contains _) + 1
    1 / rank.toDouble
  }
}