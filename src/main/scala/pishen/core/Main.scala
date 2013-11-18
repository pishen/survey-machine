package pishen.core

import java.io.PrintWriter
import org.slf4j.LoggerFactory
import resource.managed
import pishen.db.DBHandler

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")
    val source = dbHandler.records.find(r => r.fileContent.nonEmpty && r.outgoingRecords.length > 10).get
    val testCase = TestCase(source, 0.1, 50)
    printTestCase("01", testCase)
  }

  def printTestCase(id: String, testCase: TestCase) {
    for (out <- managed(new PrintWriter(id + ".html"))) {
      val res =
        <ul>
          <li>source: { testCase.source.title }</li>
          <li>seeds:</li>
          <ul>
            { testCase.seeds.map(r => <li> { r.title } </li>) }
          </ul>
          <li>answers:</li>
        </ul>
      out.println(res)
    }
  }
}