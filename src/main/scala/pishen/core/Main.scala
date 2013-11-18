package pishen.core

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import resource.managed
import pishen.db.Record

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")
    val source = dbHandler.records.find(r => r.fileContent.nonEmpty && r.outgoingRecords.length > 20).get
    val testCase = TestCase(source, 0.1, 50)
    printTestCase("test01", testCase)
  }

  def printTestCase(dirName: String, testCase: TestCase) {
    def getChange(r: Record) = {
      if (testCase.cocitationRank.contains(r)) {
        val change = testCase.cocitationRank.indexOf(r) - testCase.newCocitationRank.indexOf(r)
        if (change > 0) <span style="color:green">{ "+" + change }</span>
        else if (change < 0) <span style="color:red">{ change }</span>
        else <span style="color:black">{ change }</span>
      } else <span style="color:blue;"> new </span>
    }

    Files.createDirectory(Paths.get(dirName))
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
                  <a href="">{ r.title }</a>
                  { getChange(r) }
                </li>)
            }
          </ol>
        </ul>
      }
    }
  }
}