package text

import math._

case class Eval(ap: Double, precision: Double, recall: Double, f1: Double, ndcg: Double, rr: Double)

object Eval {
  def eval(ranks: Seq[Paper], answers: Set[Paper]) = {
    val ap = {
      val precisions = ranks.zipWithIndex.filter(answers contains _._1)
        .zipWithIndex.map(p => (p._2 + 1) / (p._1._2 + 1).toDouble)
      precisions.sum / answers.size
    }
    val hitSize = ranks.toSet.intersect(answers).size
    val precision = hitSize / ranks.size.toDouble
    val recall = hitSize / answers.size.toDouble
    val f1 = if (hitSize == 0) 0.0 else 2 * precision * recall / (precision + recall)

    val ndcg = {
      val dcg = ranks.zipWithIndex.map { case (p, i) => (p, i + 1) }
        .map { case (p, i) => (if (answers.contains(p)) 1 else 0) / log(1 + i) }.sum
      val idcg = (1 to ranks.toSet.intersect(answers).size).map(i => 1 / log(1 + i)).sum
      dcg / idcg
    }
    val rr = {
      val rank = ranks.indexWhere(answers contains _) + 1
      if (rank == 0) 0.0 else 1 / rank.toDouble
    }
    
    Eval(ap, precision, recall, f1, ndcg, rr)
  }
}