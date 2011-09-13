package sdslabs.echo.indexing



import sdslabs.echo.utils.EchoDocument
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import java.io.File
import org.apache.lucene.analysis.WhitespaceAnalyzer
import sdslabs.echo.utils.EchoConfigAccessor
import sdslabs.echo.utils.EchoLogger

class EchoIndexing() {

  val indexDirectory = EchoConfigAccessor.getString("echo.indexDirectory")
  private val indexDir : FSDirectory = FSDirectory.open(new File(indexDirectory))
  private val writer: IndexWriter = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_33), true, IndexWriter.MaxFieldLength.UNLIMITED)
  
  def indexDocument( doc: EchoDocument){
	//val writer: IndexWriter = new IndexWriter(indexDir, new WhitespaceAnalyzer(Version.LUCENE_33), true, IndexWriter.MaxFieldLength.UNLIMITED)
    EchoLogger.info("Adding document " + doc.file.getName() + " and id " + doc.docId + " to the indexes")
    writer.addDocument(doc.getDocument)
    writer.commit()
    EchoLogger.info("Finished adding document " + doc.file.getName() + " and id " + doc.docId + " to the indexes" )
  }
  
}