package pdf

import java.io.File

import scala.Array.canBuildFrom
import scala.sys.process.stringToProcess
import scala.xml.XML

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version

import main.Main.logger

object CiteSeer {
  val analyzer = new StandardAnalyzer(Version.LUCENE_46)
  val config = new IndexWriterConfig(Version.LUCENE_46, analyzer)
  val dir = FSDirectory.open(new File("citeseer-index"))

  val regex = """[^a-zA-Z\-0-9 ]"""

  "mkdir paper-pdf".!
  "mkdir citeseer-raw".!
  "mkdir citeseer-pdf".!

  def downloadRaw() = {
    def downloadRecords(index: Int, token: String): Unit = {
      val file = new File("citeseer-raw/" + index + ".xml")
      if (!file.exists()) {
        if (token == null) {
          val base = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&metadataPrefix=oai_dc"
          logger.info("curl: " + base)
          assert(Downloader.curl(base, file.getPath()) == 0)
        } else {
          val resume = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords&resumptionToken=" + token
          logger.info("curl: " + resume)
          assert(Downloader.curl(resume, file.getPath()) == 0)
        }
      }
      logger.info("check " + file.getName())
      val xml = XML.loadFile(file)
      (xml \\ "resumptionToken").headOption match {
        case None    => Unit
        case Some(n) => downloadRecords(index + 1, n.text)
      }
    }
    downloadRecords(0, null)
  }

  def createIndex(range: Range) = {
    val writer = new IndexWriter(dir, config)
    range.map(i => new File("citeseer-raw/" + i + ".xml"))
      .foreach(f => {
        logger.info("indexing " + f.getName())
        (XML.loadFile(f) \\ "dc").foreach(d => {
          val tags = Seq(d \ "title", d \ "source")
          if (tags.forall(_.nonEmpty)) {
            val title = (d \ "title").head.text.replaceAll(regex, "")
            val source = (d \ "source").head.text
            if (source.endsWith(".pdf")) {
              val doc = new Document()
              doc.add(new TextField("title", title, Store.YES))
              doc.add(new StringField("source", source, Store.YES))
              writer.addDocument(doc)
            }
          }
        })
      })
    writer.close()
  }

  def downloadPdf(dblpKey: String, title: String) = {
    val cleanTitle = title.replaceAll(regex, "")
    val reader = DirectoryReader.open(dir)
    val searcher = new IndexSearcher(reader)
    val parser = new QueryParser(Version.LUCENE_46, "title", analyzer)
    val query = parser.parse(cleanTitle)
    searcher.search(query, 1).scoreDocs
      .map(s => searcher.doc(s.doc))
      .headOption match {
        case None => false
        case Some(doc) => {
          val citeSeerTitle = doc.get("title")
          if (citeSeerTitle.toLowerCase() == cleanTitle.toLowerCase()) {
            //download source
            val source = doc.get("source")
            val res = Downloader.curl(source, "citeseer-pdf/" + dblpKey + ".pdf")
            res == 0
          }else{
            false
          }
        }
      }
  }
}