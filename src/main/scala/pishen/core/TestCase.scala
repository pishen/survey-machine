package pishen.core

import pishen.db.Record
import scala.util.Random
import scala.math._
import pishen.db.CitationMark

class TestCase(
  val filter: Record => Boolean,
  val source: Record,
  val seeds: Set[Record],
  val answers: Set[Record],
  val cocitationRank: Seq[Record],
  //val katzRank: Seq[(Record, Double)],
  val newCocitationRank: Seq[Record]) {
  val cocitationAP = computeAP(cocitationRank)
  //val katzAP = computeAP(katzRank.map(_._1))
  val newCocitationAP = computeAP(newCocitationRank)
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
      r.citationType == Record.CitationType.Number

    val shuffleRefs = Random.shuffle(source.outgoingRecords)
    val ansSize = {
      val rawSize = (shuffleRefs.size * hideRatio).toInt
      if (rawSize == 0) 1 else rawSize
    }
    val answers = shuffleRefs.take(ansSize).toSet
    val seeds = shuffleRefs.drop(ansSize)
    val seedSet = seeds.toSet

    val cocitationRank = {
      val flat = seeds.flatMap(seed =>
        seed.incomingRecords.filter(f).flatMap(cociting =>
          cociting.outgoingRecords.filter(r => r != source && !seedSet.contains(r))))
      flat.groupBy(r => r).mapValues(_.length).toSeq.sortBy(_._2).reverse.take(topK).map(_._1)
    }
    val newCocitationRank = {
      val flat = seeds.flatMap(seed => {
        seed.incomingReferences.filter(f apply _.startRecord).flatMap(ref => {
          val cociting = ref.startRecord
          cociting.outgoingReferences.filter(_.endRecord match {
            case Some(r) => r != source && !seedSet.contains(r)
            case None    => false
          }).map(targetRef => {
            val distance = ref.offsets.flatMap(offset =>
              targetRef.offsets.map(targetOffset => (targetOffset - offset).abs))
              .min / cociting.longestPairLength.toDouble
            (targetRef.endRecord.get, if (distance < 1) 1 - distance else 0)
          })
        })
      })
      flat.groupBy(_._1).mapValues(_.map(_._2).sum).toSeq.sortBy(_._2).reverse.take(topK).map(_._1)
    }

    /*def computeKatz(level: Int,
                    preLevelRecords: Seq[(Record, Int)],
                    preRankSeq: Seq[(Record, Double)]): Seq[(Record, Double)] = {
      val levelRecords = preLevelRecords
        .flatMap(p => p._1.allNeighborRecords.filter(f).map(r => (r, p._2)))
        .groupBy(_._1).mapValues(_.map(_._2).sum)
      val mergedRankSeq = (preRankSeq ++ levelRecords.mapValues(_ * pow(decay, level)).toSeq)
        .groupBy(_._1).mapValues(_.map(_._2).sum).toSeq

      if (level == katzStopLevel)
        mergedRankSeq.filterNot(seedSet contains _._1).sortBy(_._2).reverse.take(topK)
      else
        computeKatz(level + 1, levelRecords.toSeq, mergedRankSeq)
    }
    //val katzRank = computeKatz(1, seeds.map(r => (r, 1)), Seq.empty[(Record, Double)])
    val katzRank = Seq.empty[(Record, Double)]*/

    new TestCase(f, source, seedSet, answers, cocitationRank, newCocitationRank)
  }
}