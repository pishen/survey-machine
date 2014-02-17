package main

import org.slf4j.LoggerFactory
import java.io.File
import scala.xml.XML
import scala.Array.canBuildFrom

object Main {
  val logger = LoggerFactory.getLogger("main")

  def main(args: Array[String]): Unit = {
    //pdf.Tester.test()
    text.Tester.test()
  }
}