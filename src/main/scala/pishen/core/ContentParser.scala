package pishen.core

import pishen.db.Record
import pishen.db.CitationMark

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
        if(citations.isEmpty) None else Some(citations)
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

  def writeOffsetsForAllRef(record: Record) = {
    findAllCitations(record) match {
      case Some(seq) => record.outgoingReferences.foreach(ref => {
        val refIndex = ref.refIndex
        ref.writeOffsets(seq.filter(_._1 == refIndex).map(_._2))
      })
      case None => throw new IllegalArgumentException("record must have type Number")
    }
  }

}