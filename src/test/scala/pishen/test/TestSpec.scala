package pishen.test

import org.scalatest.FlatSpec
import pishen.core.TestCase

class TestSpec extends FlatSpec {
  "Cocitation" should "work" in {
    val rMap = Map(1 -> Seq(2, 3, 4, 5),
      6 -> Seq(2, 3, 4),
      7 -> Seq(3, 5))
    val testCase = TestCase(new TestRecord(1, rMap), 0.5, 100, 3, 0.05)
    expect(testCase.cocitationRank.map(p => (p._1.nodeId.toInt, p._2))) {
      Seq((3, 1), (2, 1))
    }
  }
}