package pishen.core

import pishen.db.Record
import scala.util.Random
import scala.math._
import pishen.db.CitationMark
import scala.collection.parallel.ParSeq

class TestCase(
  val source: Record,
  val answers: Set[Record],
  val cocitationRank: Seq[(Record, Int)],
  val katzRank: Seq[(Record, Double)]) {
  val cocitationAP = computeAP(cocitationRank.map(_._1))
  val katzAP = computeAP(katzRank.map(_._1))
  private def computeAP(rankSeq: Seq[Record]) = {
    val precisions = rankSeq.zipWithIndex.filter(answers contains _._1).zipWithIndex.map(p => {
      (p._2 + 1) / (p._1._2 + 1).toDouble
    })
    if (answers.size == 0) 0.0 else precisions.sum / answers.size
  }
}

object TestCase {
  def apply(source: Record, hideRatio: Double, topK: Int, katzStopLevel: Int, decay: Double) = {
    val f = (r: Record) => r != source &&
      r.year <= source.year &&
      r.citationType == CitationMark.Type.Number
    val shuffleRefs = Random.shuffle(source.outgoingRecords.filter(f))
    val ansSize = (shuffleRefs.size * hideRatio).toInt
    val answers = shuffleRefs.take(ansSize).toSet
    val seeds = shuffleRefs.drop(ansSize).par
    val seedSet = seeds.toSet
    val cocitationRank = {
      val flat = seeds.flatMap(seed =>
        seed.incomingRecords.filter(f).flatMap(middle =>
          middle.outgoingRecords.filter(r => !seedSet.contains(r) && f(r))))
      flat.groupBy(r => r).mapValues(_.length).toList.sortBy(_._2).reverse.take(topK)
    }
    def computeKatz(level: Int,
                    preLevelRecords: ParSeq[(Record, Int)],
                    preRankSeq: Seq[(Record, Double)]): Seq[(Record, Double)] = {
      val levelRecords = preLevelRecords
        .flatMap(p => p._1.allNeighborRecords.filter(f).map(r => (r, p._2)))
        .groupBy(_._1).mapValues(_.map(_._2).sum)
      val mergedRankSeq = (preRankSeq ++ levelRecords.mapValues(_ * pow(decay, level)).toSeq)
        .groupBy(_._1).mapValues(_.map(_._2).sum).toSeq
      
      if(level == katzStopLevel)
        mergedRankSeq.filterNot(seedSet contains _._1).sortBy(_._2).reverse.take(topK)
      else
        computeKatz(level + 1, levelRecords.toSeq, mergedRankSeq)
    }
    val katzRank = computeKatz(1, seeds.map(r => (r, 1)), Seq.empty[(Record, Double)])
    new TestCase(source, answers, cocitationRank, katzRank)
  }
}