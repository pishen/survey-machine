package pdf

import java.io.File
import java.io.FileWriter

import scala.Array.canBuildFrom
import scala.sys.process.stringSeqToProcess
import scala.xml.NodeSeq
import scala.xml.XML

import scalax.io.Resource

object ContentParser {
  val numberRegex = """[1-9]\d{0,2}(-[1-9]\d{0,2})?(,[1-9]\d{0,2}(-[1-9]\d{0,2})?)?""".r
  val markRegex = """\[([^\[\]]+)\]""".r

  def blockify() = {
    val outputDir = new File("paper-pdf-blockify")
    new DblpIterator().grouped(5000).foreach(seq => {
      seq.par.foreach(p => {
        val pdf = new File("paper-pdf/" + p.dblpKey + ".pdf")
        val blockFile = new File("paper-pdf-blockify/" + p.dblpKey + "_spatial.xml")
        if (pdf.exists() && !blockFile.exists()) {
          Seq("./lapdftext/blockify", pdf.getAbsolutePath(), outputDir.getAbsolutePath()).!
        }
      })
    })
  }

  def isNumericIndex(dblpKey: String) = {
    val refSize = Acm.getRefSize(dblpKey)
    val xml = XML.loadFile("paper-pdf-blockify/" + dblpKey + "_spatial.xml")
    val str = (xml \\ "Word").mkString
    val numsInStr = markRegex.findAllMatchIn(str).flatMap(m => {
      val inner = m.group(1).replaceAll("\\s", "")
      if (numberRegex.pattern.matcher(inner).matches) {
        inner.split(",").flatMap(s => {
          if (s.contains("-")) {
            val range = s.split("-").map(_.toInt)
            (range.head to range.last)
          } else {
            Seq(s.toInt)
          }
        })
      } else Seq.empty
    }).toSeq
    refSize > 0 && (1 to refSize).forall(numsInStr.contains(_))
  }

  def takeSections(paperName: String) = {
    val xml = XML.loadFile("paper-pdf-blockify/" + paperName + "_spatial.xml")
    val sections = xml.\\("Chunk").flatMap(chunk => {
      val words = chunk.\("Word")
      def split(remain: NodeSeq, splited: Seq[NodeSeq]): Seq[NodeSeq] = {
        if (remain.isEmpty) {
          splited
        } else {
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
    Resource.fromWriter(new FileWriter("paragraph-test")).write {
      sections.map(_.map(_.text).mkString(" ")).mkString("\n")
    }
  }
}