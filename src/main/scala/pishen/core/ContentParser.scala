package pishen.core

import pishen.db.Record
import pishen.db.CitationMark
import math._

object ContentParser {
  val numberRegex = """[1-9]\d{0,2}(-[1-9]\d{0,2})?(,[1-9]\d{0,2}(-[1-9]\d{0,2})?)?""".r
  val markRegex = """\[([^\[\]]+)\]""".r
  def findAllCitations(record: Record) = {
    record.fileContent match {
      case Some(c) => {
        val citations = markRegex.findAllMatchIn(c).flatMap(m => {
          val inner = m.group(1).replaceAll("\\s", "")
          if (numberRegex.pattern.matcher(inner).matches) {
            inner.split(",").flatMap(s => {
              if (s.contains("-")) {
                val range = s.split("-").map(_.toInt)
                (range.head to range.last).map(i => (i, m.start))
              } else {
                Seq((s.toInt, m.start))
              }
            })
          } else Seq.empty
        }).toSeq
        if (citations.isEmpty) None else Some(citations)
      }
      case None => None
    }
  }

  def detectType(record: Record) = {
    findAllCitations(record) match {
      case Some(seq) => {
        val parsedRefIndices = seq.map(_._1).distinct
        val refIndices = record.outgoingReferences.map(_.refIndex)
        if (parsedRefIndices.sorted == refIndices.sorted)
          Record.CitationType.Number
        else
          Record.CitationType.Unknown
      }
      case None => Record.CitationType.Unknown
    }
  }

  def writeContentInfo(record: Record) = {
    if (record.citationType == Record.CitationType.Number) {
      val citations = findAllCitations(record).get
      //write offsets for each Reference
      record.outgoingReferences.foreach(ref => {
        val refIndex = ref.refIndex
        ref.writeOffsets(citations.filter(_._1 == refIndex).map(_._2))
      })
      //write content's length
      record.writeLength(record.fileContent.get.length)
      //write longest pair length
      val longestLength = 
        citations.map(c1 => citations.filter(_._1 != c1._1).map(c2 => abs(c2._2 - c1._2)).max).max
      record.writeLongestPairLength(longestLength)
    }
  }

}