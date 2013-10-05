package pishen.core

import pishen.db.Record
import pishen.db.CitationMark

object ContentParser {
  val numberRegex = """\[[1-9]\d{0,2}(-[1-9]\d{0,2})?(,[1-9]\d{0,2}(-[1-9]\d{0,2})?)?\]""".r

  def detectType(record: Record) = {
    record.fileContent match {
      case Some(c) => {
        val refCount = numberRegex.findAllMatchIn(c.replaceAll("\\s", "")).flatMap(m => {
          m.matched.init.tail.split(",").flatMap(s => {
            if (s.contains("-")) {
              val range = s.split("-").map(_.toInt)
              (range.head to range.last)
            } else {
              Seq(s.toInt)
            }
          })
        }).toSeq.distinct.length
        if(refCount == record.allReferences.length) Some(CitationMark.Type.Number)
        else Some(CitationMark.Type.Unknown)
      }
      case None => None
    }
  }

  def findAllCitations(record: Record) = {
    record.fileContent match {
      case Some(c) =>
        Some(numberRegex.findAllMatchIn(c.replaceAll("\\s", "")).flatMap(m => {
          m.matched.init.tail.split(",").flatMap(s => {
            if (s.contains("-")) {
              val range = s.split("-").map(_.toInt)
              (range.head to range.last).map(i => (i, m.start))
            } else {
              Seq((s.toInt, m.start))
            }
          })
        }))
      case None => None
    }
  }
}