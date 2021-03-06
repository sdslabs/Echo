package sdslabs.echo.search


import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.Directory
import java.io.File
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.document.Document
import java.util.UUID
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import org.apache.lucene.analysis.WhitespaceAnalyzer
import sdslabs.echo.utils.EchoConfigAccessor
import sdslabs.echo.utils.EchoLogger

class EchoSearching() {

  val indexDirectory = EchoConfigAccessor.getString("echo.indexDirectory")
  val dir : Directory = FSDirectory.open(new File(indexDirectory));
  val is: IndexSearcher = new IndexSearcher(dir);
  
  def search(queryStr: String): Map[Float, UUID] = {
    
    val parser: QueryParser = new QueryParser(Version.LUCENE_33, "contents", new WhitespaceAnalyzer(Version.LUCENE_33));
    val query: Query = parser.parse(queryStr);
    EchoLogger.info("Searching query " + queryStr)
    val hits: TopDocs = is.search(query, 100);
    EchoLogger.info("Got " + hits.totalHits + " hits for the query " + queryStr)
    val result: Map[Float, UUID] =  new HashMap[Float, UUID]()
    hits.scoreDocs foreach ( d => {
    		val doc: Document = is.doc(d.doc)
    		result += (d.score -> UUID.fromString(doc.get("id")))
    		EchoLogger.info("Got document " + doc.get("id") + " as result for query " + queryStr)
    	}
    )
    
    return result
  }
    
}
