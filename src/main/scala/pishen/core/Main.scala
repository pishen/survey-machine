package pishen.core

import java.io.File
import java.io.FileOutputStream
import scala.Array.canBuildFrom
import scala.sys.process.stringToProcess
import org.slf4j.LoggerFactory
import pishen.db.DBHandler
import pishen.db.Record
import pishen.db.Record.CitationType.Number
import scalax.io.Resource
import math._
import java.io.FileWriter

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    val dbHandler = new DBHandler("new-graph-db")
    
    "rm -f rXY".!
    "rm -f rXnY".!
    
    dbHandler.records.filter(r => {
      println("checking " + r.name)
      r.outgoingRecords.length >= 25
    }).map(r => {
      println("create testcase")
      val pXYs = new TestCase(r, 0.2, 50).pearsonXYs
      Resource.fromFile("rXY").write(pXYs._1 + "\n")
      Resource.fromFile("rXnY").write(pXYs._2 + "\n")
    })

    /*val pearsonXYs = dbHandler.records.filter(r => {
      println("checking " + r.name)
      r.outgoingRecords.length >= 50
    }).map(r => {
      println("create testcase")
      new TestCase(r, 0.2, 50).pearsonXYs
    }).reduce((s1, s2) => s1.zip(s2).map(p => p._1 ++ p._2))*/

    /*val xAvg = pearsonXYs.map(_(0)).sum / pearsonXYs.length
    val yAvg = pearsonXYs.map(_(1)).sum / pearsonXYs.length
    val nyAvg = pearsonXYs.map(_(2)).sum / pearsonXYs.length
    val pXY = pearsonXYs.map(s => (s(0) - xAvg) * (s(1) - yAvg)).sum /
      (sqrt(pearsonXYs.map(s => pow(s(0) - xAvg, 2)).sum) * sqrt(pearsonXYs.map(s => pow(s(1) - yAvg, 2)).sum))
    val pXnY = pearsonXYs.map(s => (s(0) - xAvg) * (s(2) - nyAvg)).sum /
      (sqrt(pearsonXYs.map(s => pow(s(0) - xAvg, 2)).sum) * sqrt(pearsonXYs.map(s => pow(s(2) - nyAvg, 2)).sum))
    println("pXY: " + pXY)
    println("pXnY: " + pXnY)*/
  }

}