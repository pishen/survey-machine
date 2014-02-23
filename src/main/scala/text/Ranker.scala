package text

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
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

  def newCocitation(survey: Paper, queries: Seq[Paper]) = {

  }

  def katz(survey: Paper, queries: Seq[Paper]) = {

  }

  def newKatz(survey: Paper, queries: Seq[Paper]) = {

  }

  def rwr(survey: Paper, queries: Set[Paper], alpha: Double, k: Int) = {
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
    //adjust the walking depth here
    val subset = propagate(0, 3, queries, Set.empty)
    logger.info("rwr size: " + subset.size)
    val subseq = subset.toIndexedSeq
    val cols = subseq.map(p => {
      val neighbors = (p.outgoingPapers ++ p.incomingPapers).intersect(subset)
      val prob = if (neighbors.isEmpty) 0.0 else 1 / neighbors.size.toDouble
      subseq.map(t => if (neighbors.contains(t)) prob else 0.0)
    })
    val matrix = DenseMatrix.tabulate(subseq.size, subseq.size)((i, j) => cols(j)(i))
    val dProb = 1 / queries.size.toDouble
    val d = DenseVector(subseq.map(p => if (queries.contains(p)) dProb else 0.0).toArray)

    def iterate(oldV: DenseVector[Double], epsilon: Double): DenseVector[Double] = {
      val newV = matrix * oldV * alpha + d * (1 - alpha)
      val delta = (newV - oldV).norm(2)
      println("delta: " + delta)
      if (delta < epsilon) newV else iterate(newV, epsilon)
    }
    val initV = DenseVector(Array.fill(subseq.size)(1 / subseq.size.toDouble))
    //adjust epsilon here
    val stationalV = iterate(initV, 1.0)
    subseq.zip(stationalV.toArray)
      .filter(p => !queries.contains(p._1) && p._1.year <= survey.year)
      .sortBy(_._2)
      .reverse
      .map(_._1)
      .take(k)
  }

  def newRwr(survey: Paper, queries: Seq[Paper]) = {

  }
}