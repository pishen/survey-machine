package text

import main.Main.logger
import scalax.io.Resource
import java.io.FileWriter
import scala.util.Random

object Tester {
  case class Res(survey: Paper, coEval: Eval)

  def test(args: Array[String]) = {
    val refSizes = Paper.allPapers.map(_.outgoingPapers.size).toSeq
      .groupBy(identity).mapValues(_.size).toSeq.sortBy(_._1).reverse
    refSizes.foreach(p => logger.info(p._1 + " " + p._2))
  }

  def test2(args: Array[String]) = {

    def degreeFilter(survey: Paper) = {
      val base = survey.outgoingPapers
      //degree lower bound
      //val check1 = base.forall(p => (p.outgoingPapers ++ p.incomingPapers).size >= 5)
      //avg degree
      val check2 = base.map(p => (p.outgoingPapers ++ p.incomingPapers).size).sum / base.size.toDouble >= 20
      check2
    }

    val surveys = Paper.allPapers
      .filter(p => {
        //val conf = p.dblpKey.split("-")(1)
        p.year >= 2007 &&
          p.outgoingPapers.size >= 20 &&
          degreeFilter(p)
      }).toSeq

    val ress = surveys.flatMap(survey => {
      logger.info("test survey " + survey.dblpKey)
      val base = survey.outgoingPapers
      val baseSeq = base.toSeq
      val ansSize = (base.size * 0.5).toInt
      def validateAns(queries: Set[Paper], answers: Set[Paper]) = {
        val possibleSet = queries.flatMap(_.incomingPapers.flatMap(_.outgoingPapers))
        answers.forall(a => possibleSet.contains(a))
      }
      def findValidAns(): (Set[Paper], Set[Paper]) = {
        val answers = Random.shuffle(baseSeq).take(ansSize).toSet
        val queries = base.diff(answers)
        //if (validateAns(queries, answers)) (queries, answers) else findValidAns()
        (queries, answers)
      }
      (1 to 10).map(i => {
        val (queries, answers) = findValidAns()

        val coRanks = Ranker.cocitation(survey, queries, 50)
        //val rwrRanks = Ranker.rwr(survey, queries, args(0).toInt, args(1).toDouble, args(2).toDouble, 50)
        Res(survey, Eval.eval(coRanks, answers))
      })
    })
    val ressSize = ress.size.toDouble

    logger.info("coMAP: " + (ress.map(_.coEval.ap).sum / ressSize))
    logger.info("coMeanF1: " + (ress.map(_.coEval.f1).sum / ressSize))
    logger.info("coMeanP: " + (ress.map(_.coEval.precision).sum / ressSize))
    logger.info("coMeanP: " + (ress.map(_.coEval.recall).sum / ressSize))
  }
}