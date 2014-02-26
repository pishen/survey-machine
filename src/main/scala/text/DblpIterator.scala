package text

import scala.io.Source
import scala.xml.Node
import scala.xml.XML

class DblpIterator extends Iterator[DblpPaper] {
  private val dblp = Source.fromFile("dblp.xml").getLines
  private var buffer = parse()

  def hasNext = {
    buffer != null
  }

  def next: DblpPaper = {
    val temp = buffer
    buffer = parse()
    temp
  }

  private def parse(): DblpPaper = {
    if (dblp.hasNext) {
      val line = dblp.next
      def buildTag(tag: String) = {
        def combine(seq: Seq[String]): Seq[String] = {
          val cb = dblp.next
          if (cb.startsWith("</" + tag)) seq :+ cb
          else combine(seq :+ cb)
        }
        val start = """<?xml version="1.0" encoding="ISO-8859-1"?><!DOCTYPE dblp SYSTEM "dblp.dtd"><dblp>"""
        val end = "</dblp>"
        val node = XML.loadString(start + combine(Seq(line)).mkString + end).\\(tag).head
        new DblpPaper(node)
      }
      val paper = try {
        if (line.startsWith("<inproceedings")) {
          buildTag("inproceedings")
        } else if (line.startsWith("<article")) {
          buildTag("article")
        } else {
          null
        }
      } catch {
        case e: Exception => null
      }
      if (paper != null) paper else parse()
    } else {
      null
    }
  }
}

class DblpPaper(p: Node) {
  val dblpKey = (p \ "@key").text.replaceAll("/", "-")
  val title = (p \ "title").text
  val year = (p \ "year").text.toInt
  val ee = (p \ "ee").text
}