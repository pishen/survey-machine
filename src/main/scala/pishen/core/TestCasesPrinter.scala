package pishen.core

import sys.process._
import java.io.File
import scalax.io.Resource
import java.io.FileOutputStream
import pishen.db.Record

object TestCasesPrinter {
  def printTestCases(testCases: Seq[TestCase], minRefSize: Int) = {
    val dirName = "test-cases"
    val res = ("rm -rf " + dirName).!
    println("rm -rf " + dirName + " exit code: " + res)
    new File(dirName).mkdir()

    Resource.fromOutputStream(new FileOutputStream(dirName + "/index.html")).write {
      <ul>
        <li>min reference size of source: { minRefSize }</li>
        <li>total testCases: { testCases.length }</li>
        <li>original MAP: { testCases.map(_.cocitationAP).sum / testCases.length }</li>
        <li>new MAP: { testCases.map(_.newCocitationAP).sum / testCases.length }</li>
        <li>better:</li>
        <ol>
          {
            testCases.take(20).zipWithIndex.map(p => {
              val subDirName = dirName + "/better" + p._2
              printTestCase(subDirName, p._1)
              <li>{ p._1.source.title }<a href={ "better" + p._2 + "/root.html" }> detail </a></li>
            })
          }
        </ol>
        <li>worse:</li>
        <ol>
          {
            testCases.takeRight(20).reverse.zipWithIndex.map(p => {
              val subDirName = dirName + "/worse" + p._2
              printTestCase(subDirName, p._1)
              <li>{ p._1.source.title }<a href={ "worse" + p._2 + "/root.html" }> detail </a></li>
            })
          }
        </ol>
      </ul>.toString
    }
  }

  private def printTestCase(dirName: String, testCase: TestCase) {
    def getChange(r: Record) = {
      if (testCase.cocitationRank.contains(r)) {
        val change = testCase.cocitationRank.indexOf(r) - testCase.newCocitationRank.indexOf(r)
        if (change > 0) <span style="color:green">{ "+" + change }</span>
        else if (change < 0) <span style="color:red">{ change }</span>
        else <span style="color:black">{ change }</span>
      } else <span style="color:green;"> new </span>
    }

    new File(dirName).mkdir()
    Resource.fromOutputStream(new FileOutputStream(dirName + "/root.html")).write {
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
              <li style={ if (testCase.answers.contains(r)) "color:green;" else "" }>
                { r.title }
                { getChange(r) }
                <a href={ r.name + ".html" }> details </a>
              </li>)
          }
        </ol>
      </ul>.toString
    }

    val f = testCase.filter
    testCase.newCocitationRank.foreach { r =>
      Resource.fromOutputStream(new FileOutputStream(dirName + "/" + r.name + ".html")).write {
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
                val endIndex = shortestPair.max + 18 min cociting.fileContent.get.length
                <li>cociting: { cociting.title }</li>
                <ul>
                  <li>seed: { seedRef.refIndex }</li>
                  <li>rank: { rankRef.refIndex }</li>
                  <li>{ cociting.fileContent.get.substring(startIndex, endIndex) }</li>
                </ul>
              })
            })
          }
        </ul>.toString
      }
    }
  }
}