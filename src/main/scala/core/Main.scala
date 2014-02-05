package core

import org.slf4j.LoggerFactory
import scala.xml.XML
import scala.xml.NodeSeq
import scalax.io.Resource
import java.io.FileWriter

object Main {
  private val logger = LoggerFactory.getLogger("Main")

  def main(args: Array[String]): Unit = {
    Downloader.download()
  }

  def lapdftext() = {
    val xml = XML.loadFile("pdf/tonellotto2012selective_spatial.xml")
    val paragraphs = xml.\\("Chunk").flatMap(chunk => {
      val words = chunk.\("Word")
      def split(remain: NodeSeq, splited: Seq[NodeSeq]): Seq[NodeSeq] = {
        if(remain.isEmpty){
          splited
        }else{
          remain.indices.tail.find(i => {
            remain(i - 1) \ "@y1" != remain(i) \ "@y1" &&
            remain(i) \ "@x1" != chunk \ "@x1"
          }) match {
            case None => split(NodeSeq.Empty, splited :+ remain)
            case Some(i) => {
              val (l, r) = remain.splitAt(i)
              split(r, splited :+ l)
            }
          }
        }
      }
      split(words, Seq.empty)
    })
    Resource.fromWriter(new FileWriter("paragraph-test")).write{
      paragraphs.map(_.map(_.text).mkString(" ")).mkString("\n")
    }
  }

}