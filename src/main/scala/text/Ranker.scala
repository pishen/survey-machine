package text

import scala.math._

import main.Main.logger

object Ranker {
  def cocitation(survey: Paper, queries: Set[Paper], k: Int) = {
    queries.toSeq.flatMap(q => {
      q.incomingPapers
        .filter(_ != survey)
        .toSeq
        .flatMap(_.outgoingPapers)
        .filter(p => p != survey && p.year <= survey.year && !queries.contains(p))
    }).groupBy(identity)
      .mapValues(_.size)
      .toSeq
      .sortBy(_._2)
      .reverse
      .map(_._1)
      .take(k)
  }

  def newCocitation(survey: Paper, queries: Set[Paper], k: Int) = {
    queries.toSeq.flatMap(q => {
      q.incomingRefs.toSeq.flatMap(qRef => {
        val citing = qRef.startPaper
        if (citing != survey) {
          val citations = citing.citations
          val qIndex = qRef.index
          val qOffsets = citations.filter(_.index == qIndex).map(_.offset)
          citing.outgoingRefs.flatMap(tRef => {
            val target = tRef.endPaper
            if (target != survey && target.year <= survey.year && !queries.contains(target)) {
              val tIndex = tRef.index
              val tOffsets = citations.filter(_.index == tIndex).map(_.offset)
              Some((target, if (qOffsets.intersect(tOffsets).nonEmpty) 5 else 1))
            } else None
          })
        } else None
      }).groupBy(_._1)
        .mapValues(_.map(_._2).sum)
        .toSeq
        .sortBy(_._2)
        .reverse
        .map(_._1)
        .take(k)
    })
  }

  def katz(survey: Paper, queries: Seq[Paper]) = {

  }

  def newKatz(survey: Paper, queries: Seq[Paper]) = {

  }

  def propagate(survey: Paper, level: Int, limit: Int,
                levelPapers: Set[Paper], passedPapers: Set[Paper]): Set[Paper] = {
    if (level == limit) {
      levelPapers ++ passedPapers
    } else {
      val expanded = levelPapers
        .flatMap(p => p.outgoingPapers ++ p.incomingPapers)
        .filter(_ != survey)
      val newPassed = passedPapers ++ levelPapers
      val newLevel = expanded diff newPassed
      propagate(survey, level + 1, limit, newLevel, newPassed)
    }
  }

  def rwr(survey: Paper, queries: Set[Paper], depth: Int, alpha: Double, epsilon: Double, k: Int) = {
    //get the sub-graph, adjust the walking depth here
    val subset = propagate(survey, 0, depth, queries, Set.empty)
    def getNeighbors(p: Paper) = (p.incomingPapers ++ p.outgoingPapers).intersect(subset)
    println("rwr size: " + subset.size)

    val probMap = subset.map(p => {
      val neighbors = getNeighbors(p)
      val prob = if (neighbors.isEmpty) 0.0 else 1 / neighbors.size.toDouble
      p -> prob
    }).toMap

    val restartProb = (1 / queries.size.toDouble) * (1 - alpha)
    def iterate(oldPRs: Map[Paper, Double]): Map[Paper, Double] = {
      val newPRs = subset.map(p => {
        val fromWalk = getNeighbors(p).map(n => oldPRs(n) * probMap(n)).sum * alpha
        val fromRestart = if (queries.contains(p)) restartProb else 0.0
        p -> (fromWalk + fromRestart)
      }).toMap
      val l2 = sqrt(newPRs.map { case (p, pr) => pow(pr - oldPRs(p), 2) }.sum)
      println("l2: " + l2)
      if (l2 < epsilon) newPRs else iterate(newPRs)
    }

    val initPRs = subset.map(_ -> 1 / subset.size.toDouble).toMap
    //adjust epsilon here
    iterate(initPRs)
      .filterKeys(p => !queries.contains(p) && p.year <= survey.year)
      .toSeq
      .sortBy(_._2)
      .reverse
      .map(_._1)
      .take(k)
  }

  def newRwr(survey: Paper, queries: Seq[Paper]) = {

  }
}