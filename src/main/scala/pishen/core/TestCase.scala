package pishen.core

import pishen.db.Record
import scala.util.Random
import scala.math._
import pishen.db.CitationMark

class TestCase(val source: Record, hideRatio: Double, topK: Int, stopLevel: Int = 3, decay: Double = 0.03) {
  val filter = (r: Record) => r != source &&
    r.year <= source.year &&
    r.citationType == Record.CitationType.Number

  private val srcRecords = source.outgoingRecords
  private val ansSize = {
    val rawSize = (srcRecords.size * hideRatio).toInt
    if (rawSize == 0) 1 else rawSize
  }
  val (answers, seeds) = Random.shuffle(srcRecords).splitAt(ansSize)

  //cocitation
  lazy val cocitationRank = {
    val flat = seeds.flatMap(seed =>
      seed.incomingRecords.filter(filter).flatMap(cociting =>
        cociting.outgoingRecords.filter(r => r != source && !seeds.contains(r))))
    flat.groupBy(r => r).mapValues(_.length).toSeq.sortBy(_._2).reverse.take(topK).map(_._1)
  }
  lazy val newCocitationRank = {
    println("not lazy")
    val flat = seeds.flatMap(seed => {
      seed.incomingReferences.filter(filter apply _.startRecord).flatMap(ref => {
        val cociting = ref.startRecord
        cociting.outgoingReferences.filter(_.endRecord match {
          case Some(r) => r != source && !seeds.contains(r)
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

  //katz
  /*private def computeKatz(level: Int,
                          preLevelRecords: Seq[(Record, Int)],
                          preRankSeq: Seq[(Record, Double)],
                          stopLevel: Int,
                          decay: Double): Seq[(Record, Double)] = {
    val levelRecords = preLevelRecords
      .flatMap(p => p._1.allNeighborRecords.filter(filter).map(r => (r, p._2)))
      .groupBy(_._1).mapValues(_.map(_._2).sum)
    val mergedRankSeq = (preRankSeq ++ levelRecords.mapValues(_ * pow(decay, level)).toSeq)
      .groupBy(_._1).mapValues(_.map(_._2).sum).toSeq

    if (level == stopLevel)
      mergedRankSeq.filterNot(seeds contains _._1).sortBy(_._2).reverse.take(topK)
    else
      computeKatz(level + 1, levelRecords.toSeq, mergedRankSeq, stopLevel, decay)
  }
  lazy val katzRank =
    computeKatz(1, seeds.map(r => (r, 1)), Seq.empty[(Record, Double)], stopLevel, decay)*/

  //pearson
  def pearsonXYs = {
    val pairs = srcRecords.flatMap(_.incomingRecords.filter(filter)).distinct
      .flatMap(_.outgoingRecords.filter(_ != source)).distinct
      .combinations(2).map(s => (s.head, s.last)).toSeq
    val xs = pairs.map(p => if (srcRecords.contains(p._1) && srcRecords.contains(p._2)) 1 else 0)
    val xAvg = xs.sum / xs.length.toDouble
    val ys = pairs.map(p => {
      p._1.incomingRecords.filter(filter).intersect(p._2.incomingRecords.filter(filter)).length
    })
    val yAvg = ys.sum / ys.length.toDouble
    val nys = pairs.map(p => {
      val scores = p._1.incomingRecords.filter(filter).intersect(p._2.incomingRecords.filter(filter))
        .map(cociting => {
          val pairRefs = cociting.outgoingReferences.filter(_.endRecord match {
            case Some(r) => r == p._1 || r == p._2
            case None    => false
          })
          val ref = pairRefs.head
          val targetRef = pairRefs.last
          val longestPairLength = if(cociting.longestPairLength > 0) cociting.longestPairLength else 1
          val distance = ref.offsets.flatMap(offset =>
            targetRef.offsets.map(targetOffset => (targetOffset - offset).abs))
            .min / longestPairLength.toDouble
          1 - distance
        })
      if(scores.isEmpty) 0.0 else scores.sum
    })
    val nyAvg = nys.sum / nys.length
    
    println("nys.sum: " + nys.sum)
    println("length: " + nys.length)
    println("nyAvg: " + nyAvg)
    val sum = nys.map(ny => pow(ny - nyAvg, 2)).sum
    val sq = sqrt(sum)
    println("sum: " + sum)
    println("sqrt: " + sq)

    val pXY = xs.map(_ - xAvg).zip(ys.map(_ - yAvg)).map(p => p._1 * p._2).sum /
      (sqrt(xs.map(x => pow(x - xAvg, 2)).sum) * sqrt(ys.map(y => pow(y - yAvg, 2)).sum))
    val pXnY = xs.map(_ - xAvg).zip(nys.map(_ - nyAvg)).map(p => p._1 * p._2).sum /
      (sqrt(xs.map(x => pow(x - xAvg, 2)).sum) * sqrt(nys.map(ny => pow(ny - nyAvg, 2)).sum))

    (pXY, pXnY)
    //Seq(xs.map(_.toDouble), ys.map(_.toDouble), nys)
  }

  //AP
  private def computeAP(rankSeq: Seq[Record]) = {
    val precisions = rankSeq.zipWithIndex.filter(answers contains _._1).zipWithIndex.map(p => {
      (p._2 + 1) / (p._1._2 + 1).toDouble
    })
    if (answers.size == 0) 0.0 else precisions.sum / answers.size
  }
  lazy val cocitationAP = computeAP(cocitationRank)
  //lazy val katzAP = computeAP(katzRank.map(_._1))
  lazy val newCocitationAP = computeAP(newCocitationRank)
}
