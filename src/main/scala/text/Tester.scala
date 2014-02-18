package text

import main.Main.logger
import scalax.io.Resource
import java.io.FileWriter

object Tester {
  def test() = {
    val surveys = Paper.allPapers
      .filter(p => {
        val conf = p.dblpKey.split("-")(1)
        p.title.toLowerCase().contains("survey") &&
        (conf == "soda" || conf == "www" || conf == "sigir" || conf == "cikm" || conf == "kdd")
      }).toSeq
    logger.info("paper contain survey: " + surveys.size)
    
    val str = surveys.map(p => {
      "conf: " + p.dblpKey.split("-")(1) + "\n" +
      "title: " + p.title + "\n" +
      p.ee
    }).mkString("\n")
    Resource.fromWriter(new FileWriter("surveys.txt")).write(str)

    /*Paper.allPapers
      .filter(p => {
        p.title.toLowerCase().contains("survey")
      }).foreach(survey => {
        val f = (p: Paper) => p.year < survey.year
        val relatedGroup = survey.outgoingPapers
        val ansSize = (relatedGroup.size * 0.1).toInt
        relatedGroup.grouped(ansSize).filter(_.size == ansSize)
          .foreach(answers => {
            val queries = relatedGroup.diff(answers)
            queries.flatMap(q => {

            })
          })
      })*/
  }
}