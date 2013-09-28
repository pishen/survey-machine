package pishen.core

import pishen.db.Record
import scala.util.Random
import pishen.db.CitationMark

class TestCase(
  val source: Record,
  val answers: Set[Record],
  val seeds: Seq[Record],
  val cocitationRank: Seq[(Record, Int)]) {
  val cocitationAP = computeAP(cocitationRank.map(_._1))
  private def computeAP(rankSeq: Seq[Record]) = {
    val precisions = rankSeq.zipWithIndex.filter(answers contains _._1).zipWithIndex.map(p => {
      (p._2 + 1) / (p._1._2 + 1).toDouble
    })
    if (answers.size == 0) 0.0 else precisions.sum / answers.size
  }
}

object TestCase {
  def apply(source: Record, hideRatio: Double, topK: Int) = {
    val f = (r: Record) => r != source &&
      r.year <= source.year &&
      r.citationType == CitationMark.Type.Number
    val shuffleRefs = Random.shuffle(source.outgoingRecords.filter(f))
    val distinctL = shuffleRefs.distinct.length
    assert(distinctL == shuffleRefs.length, distinctL + " do not equal " + shuffleRefs.length)
    val ansSize = (shuffleRefs.size * hideRatio).toInt
    val answers = shuffleRefs.take(ansSize).toSet
    val seeds = shuffleRefs.drop(ansSize)
    val seedSet = seeds.toSet
    val cocitationRank = {
      val flat = seeds.flatMap(seed =>
        seed.incomingRecords.filter(f).flatMap(middle =>
          middle.outgoingRecords.filter(r => !seedSet.contains(r) && f(r))))
      flat.groupBy(r => r).mapValues(_.length).toSeq.sortBy(_._2).reverse.take(topK)
    }
    new TestCase(source, answers, seeds, cocitationRank)
  }
}