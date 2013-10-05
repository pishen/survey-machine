package pishen.core

import pishen.db.Record

object ContentParser {
  val numberRegex = """\[[1-9]\d{0,2}(-[1-9]\d{0,2})?(,[1-9]\d{0,2}(-[1-9]\d{0,2})?)?\]""".r

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