package pishen.core

import pishen.db.Record
import scala.util.Random

class TestCase() {
  
}

object TestCase {
  def apply(source: Record, hideRatio: Double, f: Record => Boolean){
    val randomRefs = Random.shuffle(source.outgoingRecords.filter(f))
    val ansSize = (randomRefs.size * hideRatio).toInt
    val answers = randomRefs.take(ansSize)
    val seeds = randomRefs.drop(ansSize)
    //val cocitationRank = //TODO implement Record's ==
  }
}