package text

import main.Main.logger
import scalax.io.Resource
import java.io.FileWriter
import scala.util.Random

object Tester {
  case class Res(survey: Paper,
                 coAP: Double, coF1: Double, coRR: Double,
                 rwrAP: Double, rwrF1: Double, rwrRR: Double)

  def test(args: Array[String]) = {
    /*def cociteSearch(survey: Paper, seeds: Seq[Paper], used: Seq[Paper]): Seq[Paper] = {
      val larger = seeds.flatMap(_.incomingPapers.filter(_ != survey).flatMap(_.outgoingPapers)).distinct
      val largerSeeds = larger.intersect(survey.outgoingPapers).diff(used)
      if (largerSeeds.size == seeds.size) used ++ seeds
      else cociteSearch(survey, largerSeeds.diff(seeds), used ++ seeds)
    }*/
    def degreeFilter(survey: Paper) = {
      val base = survey.outgoingPapers
      val avgDegree = base.map(p => (p.outgoingPapers ++ p.incomingPapers).size).sum / base.size.toDouble
      //adjust the threshold here
      avgDegree >= 20.0
    }

    val surveys = Paper.allPapers
      .filter(p => {
        //val conf = p.dblpKey.split("-")(1)
        p.year >= 2007 &&
          p.outgoingPapers.size >= 20 &&
          degreeFilter(p)
        /*(conf == "wsdm" || conf == "www" || conf == "sigir" || conf == "cikm" || conf == "kdd")*/
      }).toSeq.take(1)

    val ress = surveys.flatMap(survey => {
      println("testing on survey " + survey.dblpKey)
      val base = survey.outgoingPapers
      val baseSeq = base.toSeq
      val ansSize = (base.size * 0.1).toInt
      (1 to 10).map(i => {
        val answers = Random.shuffle(baseSeq).take(ansSize).toSet
        val queries = base.diff(answers)
        val coRanks = Ranker.cocitation(survey, queries, 50)
        val rwrRanks = Ranker.rwr(survey, queries, args(0).toInt, args(1).toDouble, args(2).toDouble, 50)
        Res(survey,
          Eval.computeAP(coRanks, answers),
          Eval.computeF1(coRanks, answers),
          Eval.computeRR(coRanks, answers),
          Eval.computeAP(rwrRanks, answers),
          Eval.computeF1(rwrRanks, answers),
          Eval.computeRR(rwrRanks, answers))
      })
    })
    val ressSize = ress.size.toDouble
    val coMAP = ress.map(_.coAP).sum / ressSize
    val coMeanF1 = ress.map(_.coF1).sum / ressSize
    val coMRR = ress.map(_.coRR).sum / ressSize
    val rwrMAP = ress.map(_.rwrAP).sum / ressSize
    val rwrMeanF1 = ress.map(_.rwrF1).sum / ressSize
    val rwrMRR = ress.map(_.rwrRR).sum / ressSize
    logger.info("coMAP: " + coMAP)
    logger.info("coMeanF1: " + coMeanF1)
    logger.info("coMRR: " + coMRR)
    logger.info("rwrMAP: " + rwrMAP)
    logger.info("rwrMeanF1: " + rwrMeanF1)
    logger.info("rwrMRR: " + rwrMRR)
    /*logger.info("top coAPs:")
    ress.sortBy(_.coAP).reverse.take(20).foreach(r => {
      logger.info(r.survey.title)
      logger.info("coAP: " + r.coAP)
    })
    logger.info("top rwrAPs:")
    ress.sortBy(_.rwrAP).reverse.take(20).foreach(r => {
      logger.info(r.survey.title)
      logger.info("rwrAP: " + r.rwrAP)
    })*/
  }
}