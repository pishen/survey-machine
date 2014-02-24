package text

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import main.Main.logger
import math._

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

  def newCocitation(survey: Paper, queries: Seq[Paper]) = {

  }

  def katz(survey: Paper, queries: Seq[Paper]) = {

  }

  def newKatz(survey: Paper, queries: Seq[Paper]) = {

  }

  def rwr(survey: Paper, queries: Set[Paper], alpha: Double, epsilon: Double, k: Int) = {
    def propagate(level: Int, limit: Int, levelPapers: Set[Paper], passedPapers: Set[Paper]): Set[Paper] = {
      if (level == limit) {
        levelPapers ++ passedPapers
      } else {
        val expanded = levelPapers
          .flatMap(p => p.outgoingPapers ++ p.incomingPapers)
          .filter(_ != survey)
        val newPassed = passedPapers ++ levelPapers
        val newLevel = expanded diff newPassed
        propagate(level + 1, limit, newLevel, newPassed)
      }
    }
    //get the sub-graph, adjust the walking depth here
    val subset = propagate(0, 3, queries, Set.empty)
    def getNeighbors(p: Paper) = (p.incomingPapers ++ p.outgoingPapers).intersect(subset)
    logger.info("rwr size: " + subset.size)

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