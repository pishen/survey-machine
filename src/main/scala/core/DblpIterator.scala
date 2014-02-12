package core

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
    new DblpPaper(temp)
  }

  private def parse(): Node = {
    if (dblp.hasNext) {
      val line = dblp.next
      if (line.startsWith("<inproceedings")) {
        def combine(seq: Seq[String]): Seq[String] = {
          val cb = dblp.next
          if (cb.startsWith("</inproceedings")) seq :+ cb
          else combine(seq :+ cb)
        }
        val start = """<?xml version="1.0" encoding="ISO-8859-1"?><!DOCTYPE dblp SYSTEM "dblp.dtd"><dblp>"""
        val end = "</dblp>"
        XML.loadString(start + combine(Seq(line)).mkString + end).\\("inproceedings").head
      } else {
        parse()
      }
    }else{
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