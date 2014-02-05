package core

object ContentParser {
  val numberRegex = """[1-9]\d{0,2}(-[1-9]\d{0,2})?(,[1-9]\d{0,2}(-[1-9]\d{0,2})?)?""".r
  val markRegex = """\[([^\[\]]+)\]""".r
  
  def blockify(filename: String) = {
    
  }
}