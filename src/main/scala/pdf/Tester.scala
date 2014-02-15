package pdf

object Tester {
  def test() = {
    Downloader.indexCiteSeer(100)
    Downloader.findFromCiteSeer("MATHEMATICS OF COMPUTATION")
  }
}