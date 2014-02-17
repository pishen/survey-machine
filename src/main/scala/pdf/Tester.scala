package pdf

import main.Main.logger

object Tester {
  def test() = {
    //CiteSeer.createIndex(0 to 5100)
    new DblpIterator().filter(p => {
      p.year >= 2006 && p.year < 2010 && p.ee.startsWith("http://doi.acm.org")
    }).foreach(p => {
      logger.info("paper: " + p.dblpKey)
      val resAcm = Acm.download(p.dblpKey, p.ee)
      val resPdf = CiteSeer.downloadPdf(p.dblpKey, p.title)
      if(resAcm || resPdf){
        Downloader.switchPort()
        Thread.sleep(5000)
      }
    })
  }
}