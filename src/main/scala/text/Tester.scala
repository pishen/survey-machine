package text

import main.Main.logger

object Tester {
  def test() = {
    val surveys = Paper.allPapers
      .filter(p => {
        p.title.toLowerCase().contains("survey")
      }).toSeq
    logger.info("paper contain survey: " + surveys.size)
    
    val confs = surveys.map(_.dblpKey.split("-")(1)).distinct.mkString(" ")
    logger.info(confs)

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