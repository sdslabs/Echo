package sdslabs.echo.general



import akka.actor.Actor
import Actor._
import sdslabs.echo.dataprocessing._
import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBObject

import sdslabs.echo.utils._
import sdslabs.echo.search._
import java.io.File
import java.util.UUID
import sdslabs.echo.indexing._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import sdslabs.echo.recommendation.EchoRecommender
import com.mongodb.DBCollection

class EchoController extends Actor{
  
  val ipAdd = EchoConfigAccessor.getString("echo.db.ipAdd")
  val m = new Mongo(ipAdd)
  val dbName = EchoConfigAccessor.getString("echo.db")
  val dataStore = m.getDB(dbName)
 
  val clusterer = actorOf(new EchoClustering(dataStore)).start()
  EchoLogger.debug("Started EchoClustering actor")
  
  val indexing : EchoIndexing = new EchoIndexing()
  EchoLogger.debug("Instantiated EchoIndexing")
  
  val extracter: EchoExtraction = new EchoExtraction();
  EchoLogger.debug("Instatiated EchoExtraction")
  
  val recommender : EchoRecommender = new EchoRecommender(dataStore)
  EchoLogger.debug("Instatiated EchoRecommender")
  
  val searcher : EchoSearching = new EchoSearching()
  EchoLogger.debug("Instatiated EchoSearcher")
  
  def uploadToDataStore(info : Map[String, String] ){  
    val doc : DBObject = extracter.toDBObject(info)
    val ds = m.getDB(dbName)
    val collectionName = EchoConfigAccessor.getString("echo.db.collectionName")
    val coll : DBCollection = ds.getCollection(collectionName)
    coll.insert(doc)
    EchoLogger.info("Added a new book to db " + info.toString())
  }
   
  def receive = {
    
	case EchoMessage.StartClustering => {
	  EchoLogger.info("Clustering Started")
	  clusterer ! EchoMessage.EchoReClusterAll()
	  EchoLogger.info("Clustering Done")
	}
	case EchoMessage.Query(query : String) => {
	  EchoLogger.info("Got query " + query +  "for searching")
	  self.reply(EchoMessage.QueryReply(searcher.search(query)))
	}
	case EchoMessage.AddDocument(location: String) => {
	  
	  EchoLogger.info("Adding new document at "+ location + " to the indexes")
	  val file : File = new File(location)
	  val info : Map[String,String] = extracter.getInfo(file.getName())
	  
	  val uuid : UUID = UUID.fromString( UUID.randomUUID().toString )
	  
	  EchoLogger.info("The id for the document at " + location + " is " + uuid)
	  
	  val doc: EchoDocument = new EchoDocument(file, uuid)
	  indexing.indexDocument(doc)
	  info.put("uuid", uuid.toString)
	  uploadToDataStore(info)
	  
	  EchoLogger.info("Completed adding document with id " + uuid )
	  
	}
	case EchoMessage.GetRecommendation(uuid: String) => {
	  EchoLogger.info("Getting recommendation for doc with id " + uuid)
	  self.reply(EchoMessage.RecommenderReply(recommender.getSimilarDocument(uuid)))
	  EchoLogger.info("Successfully sent recommendation for doc with id " + uuid)
	}
	case unwantedMessages => {
	  EchoLogger.fatal("Unrecognized error" + unwantedMessages)
	}
  }
	
}