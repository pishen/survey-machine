package text

import main.Main.logger
import scalax.io.Resource
import java.io.FileWriter
import scala.util.Random
import org.rogach.scallop.ScallopConf

object Tester {
  class Conf(args: Seq[String]) extends ScallopConf(args) {
    val hide = opt[Double](required = true)
    val rwr = toggle()
    val depth = opt[Int]()
    val alpha = opt[Double]()
    val epsilon = opt[Double]()
    dependsOnAll(rwr, List(depth, alpha, epsilon))
  }

  case class Res(survey: Paper, coEval: Eval, newCoEval: Eval, rwrEval: Option[Eval])

  def test(args: Array[String]) = {
    val conf = new Conf(args)

    def degreeFilter(survey: Paper) = {
      val base = survey.outgoingPapers
      //degree lower bound
      val check1 = base.forall(p => (p.outgoingPapers ++ p.incomingPapers).size >= 5)
      //avg degree
      val check2 = base.map(p => (p.outgoingPapers ++ p.incomingPapers).size).sum / base.size.toDouble >= 20
      check1 && check2
    }

    val surveys = Paper.allPapers
      .filter(p => {
        //val conf = p.dblpKey.split("-")(1)
        p.year >= 2007 &&
          p.outgoingPapers.size >= 20
        degreeFilter(p)
      }).toSeq

    val ress = surveys.flatMap(survey => {
      logger.info("test survey " + survey.dblpKey)
      val base = survey.outgoingPapers
      val baseSeq = base.toSeq
      val ansSize = (base.size * conf.hide()).toInt

      (1 to 10).flatMap(i => {
        val answers = Random.shuffle(baseSeq).take(ansSize).toSet
        val queries = base.diff(answers)

        val coEval = Eval.eval(Ranker.cocitation(survey, queries, 50), answers)
        val newCoEval = Eval.eval(Ranker.newCocitation(survey, queries, 50), answers)

        val rwrEval = if (conf.rwr()) {
          val ranks = Ranker.rwr(survey, queries, conf.depth(), conf.alpha(), conf.epsilon(), 50)
          Some(Eval.eval(ranks, answers))
        } else None

        Some(Res(survey, coEval, newCoEval, rwrEval))

        /*val possibleSet = queries.flatMap(_.incomingPapers.filter(_ != survey).flatMap(_.outgoingPapers))
        if (answers.forall(a => possibleSet.contains(a))) {
          val coRanks = Ranker.cocitation(survey, queries, 50)
          val rwrRanks = Ranker.rwr(survey, queries, args(0).toInt, args(1).toDouble, args(2).toDouble, 50)
          Some(Res(survey, Eval.eval(coRanks, answers), Eval.eval(rwrRanks, answers)))
        } else {
          None
        }*/
      })
    })
    val ressSize = ress.size.toDouble

    logger.info("ress-size: " + ress.size)
    logger.info("coMAP: " + (ress.map(_.coEval.ap).sum / ressSize))
    logger.info("coMeanF1: " + (ress.map(_.coEval.f1).sum / ressSize))
    logger.info("coMeanP: " + (ress.map(_.coEval.precision).sum / ressSize))
    logger.info("coMeanR: " + (ress.map(_.coEval.recall).sum / ressSize))
    logger.info("newCoMAP: " + (ress.map(_.newCoEval.ap).sum / ressSize))
    logger.info("newCoMeanF1: " + (ress.map(_.newCoEval.f1).sum / ressSize))
    logger.info("newCoMeanP: " + (ress.map(_.newCoEval.precision).sum / ressSize))
    logger.info("newCoMeanR: " + (ress.map(_.newCoEval.recall).sum / ressSize))
    if (conf.rwr()) {
      logger.info("rwrMAP: " + (ress.map(_.rwrEval.get.ap).sum / ressSize))
      logger.info("rwrMeanF1: " + (ress.map(_.rwrEval.get.f1).sum / ressSize))
      logger.info("rwrMeanP: " + (ress.map(_.rwrEval.get.precision).sum / ressSize))
      logger.info("rwrMeanR: " + (ress.map(_.rwrEval.get.recall).sum / ressSize))
    }
  }
}