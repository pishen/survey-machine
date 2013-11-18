package pishen.core

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import resource.managed
import pishen.db.Record
import java.io.File

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")
    val source = dbHandler.records.find(r => r.fileContent.nonEmpty && r.outgoingRecords.length > 20).get
    val testCase = TestCase(source, 0.1, 50)

    val dirName = "test-cases"
    val dir = new File(dirName)
    if (!dir.exists) dir.mkdir()

    printTestCase(dirName + "/test01", testCase)
  }

  def printTestCase(dirName: String, testCase: TestCase) {
    def getChange(r: Record) = {
      if (testCase.cocitationRank.contains(r)) {
        val change = testCase.cocitationRank.indexOf(r) - testCase.newCocitationRank.indexOf(r)
        if (change > 0) <span style="color:green">{ "+" + change }</span>
        else if (change < 0) <span style="color:red">{ change }</span>
        else <span style="color:black">{ change }</span>
      } else <span style="color:green;"> new </span>
    }

    val dir = new File(dirName)
    if (!dir.exists) dir.mkdir()
    for (out <- managed(new PrintWriter(dirName + "/root.html"))) {
      out.println {
        <ul>
          <li>source: { testCase.source.title }</li>
          <li>cocitationAP: { testCase.cocitationAP }</li>
          <li>newCocitationAP: { testCase.newCocitationAP }</li>
          <li>seeds:</li>
          <ul>
            { testCase.seeds.map(r => <li> { r.title } </li>) }
          </ul>
          <li>answers:</li>
          <ul>
            { testCase.answers.map(r => <li> { r.title } </li>) }
          </ul>
          <li>cocitation:</li>
          <ol>
            {
              testCase.cocitationRank.map(r =>
                if (testCase.answers.contains(r)) <li style="color:green;">{ r.title }</li>
                else <li>{ r.title }</li>)
            }
          </ol>
          <li>new cocitation:</li>
          <ol>
            {
              testCase.newCocitationRank.map(r =>
                <li styple={ if (testCase.answers.contains(r)) "color:green;" else "" }>
                  { r.title }
                  { getChange(r) }
                  <a href={ r.name + ".html" }> details </a>
                </li>)
            }
          </ol>
        </ul>
      }
    }

    val f = testCase.filter
    testCase.newCocitationRank.foreach(r => {
      for (out <- managed(new PrintWriter(dirName + "/" + r.name + ".html"))) {
        out.println {
          <ul>
            {
              r.incomingReferences.filter(ref => f(ref.startRecord)).flatMap(rankRef => {
                val cociting = rankRef.startRecord
                cociting.outgoingReferences.filter(ref => {
                  val target = ref.endRecord
                  target.nonEmpty && testCase.seeds.contains(target.get)
                }).map(seedRef => {
                  val shortestPair =
                    seedRef.offsets.flatMap(seed => rankRef.offsets.map(rank => Seq(seed, rank)))
                      .minBy(s => (s.head - s.last).abs)
                  val startIndex = shortestPair.min - 15 max 0
                  val endIndex = shortestPair.max + 15 min cociting.fileContent.get.length
                  <li>cociting: { cociting.title }</li>
                  <ul>
                    <li>seed: { seedRef.refIndex }</li>
                    <li>rank: { rankRef.refIndex }</li>
                    <li>{ cociting.fileContent.get.substring(startIndex, endIndex) }</li>
                  </ul>
                })
              })
            }
          </ul>
        }
      }
    })
  }
}