package text

object Ranker {
  def cocitation(survey: Paper, queries: Seq[Paper], k: Int) = {
    queries.flatMap(q => {
      q.incomingPapers
        .filter(_ != survey)
        .flatMap(_.outgoingPapers.filter(p => p != survey && !queries.contains(p)))
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

  def rwr(survey: Paper, queries: Seq[Paper]) = {
    
  }
  
  def newRwr(survey: Paper, queries: Seq[Paper]) = {
    
  }
}