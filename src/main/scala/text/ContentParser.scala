package text

object ContentParser {
  val numberRegex = """[1-9]\d{0,2}(-[1-9]\d{0,2})?(,[1-9]\d{0,2}(-[1-9]\d{0,2})?)?""".r
  val markRegex = """\[([^\[\]]+)\]""".r

  case class Citation(index: Int, offset: Int)

  def findAllCitations(content: String) = {
    markRegex.findAllMatchIn(content).flatMap(m => {
      val inner = m.group(1).replaceAll("\\s", "")
      if (numberRegex.pattern.matcher(inner).matches) {
        inner.split(",").flatMap(s => {
          if (s.contains("-")) {
            val range = s.split("-").map(_.toInt)
            (range.head to range.last).map(i => Citation(i, m.start))
          } else {
            Some(Citation(s.toInt, m.start))
          }
        })
      } else None
    }).toSeq
  }
}