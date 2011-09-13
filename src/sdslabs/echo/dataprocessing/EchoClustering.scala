package sdslabs.echo.dataprocessing

import com.aliasi.classify.PrecisionRecallEvaluation
import com.aliasi.cluster.HierarchicalClusterer
import com.aliasi.cluster.ClusterScore
import com.aliasi.cluster.CompleteLinkClusterer
import com.aliasi.cluster.SingleLinkClusterer
import com.aliasi.cluster.Dendrogram
import com.aliasi.util.Counter
import com.aliasi.util.Distance
import com.aliasi.util.Files
import com.aliasi.util.ObjectToCounterMap
import com.aliasi.util.Strings
import com.aliasi.tokenizer._

import sdslabs.echo.utils.EchoClusteringDocument

import com.mongodb._

import akka.actor.Actor
import sdslabs.echo.utils._
import java.util.HashSet
import java.util.Set
import java.util.UUID
import java.io._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap


/*
 * The worst clustering I can do for Echo.. 
 * But this should satisfy the current need
 * more like a do it all again... will surely not scale to large number of books
 * So run it at night only... hehehehehe
 */
class EchoClustering(dataStore: DB ) extends Actor{
  
  def getUUID(name : String):UUID = {
    val collectionName = EchoConfigAccessor.getString("echo.db.collectionName")
    val coll : DBCollection = dataStore.getCollection(collectionName)
    val query : BasicDBObject = new BasicDBObject()
	query.put("name", name)
	val cur = coll.find(query)
	var id: String = ""
	while(cur.hasNext){
	  id = cur.next().get("uuid").asInstanceOf[String]
	}
	EchoLogger.info("Got uuid " + id + " for name " + name)
    return UUID.fromString(id)
  }
  
  def performClustering(): Set[Set[EchoClusteringDocument]] = {
    
    val docDir = EchoConfigAccessor.getString("clustering.docDir")
    EchoLogger.info("Performing clustering on directory " + docDir)
    val dir : File = new File(docDir)
	var refPar : Set[Set[EchoClusteringDocument]] = new HashSet[Set[EchoClusteringDocument]]
	var docSet: Set[EchoClusteringDocument] = new HashSet[EchoClusteringDocument]
    
    for( file <- dir.listFiles()){
      val uuid = getUUID(file.getName)
      EchoLogger.info("Adding file " + file.getName() + " with id " + uuid + "to the cluster")
      try {
    	  docSet.add( new EchoClusteringDocument(file, uuid))
      }catch {
        case e => {
          EchoLogger.error("Exception thrown skip this document " + e)
        }
      }
    }
    
    val cosine_dist : Distance[EchoClusteringDocument] = new Distance[EchoClusteringDocument](){
      def distance(doc1: EchoClusteringDocument, doc2: EchoClusteringDocument): Double = {
        val ret: Double = 1.0 - doc1.cosine(doc2)
        if( ret > 1.0){
          return 1.0
        }else if ( ret < 0.0) {
          return 0.0
        }else {
          return ret
        }
      }
    }
    EchoLogger.info("Heirarchical Clustering is started")
    val clClusterer: HierarchicalClusterer[EchoClusteringDocument] = 
      new CompleteLinkClusterer[EchoClusteringDocument](cosine_dist)
    val dendrogram: Dendrogram[EchoClusteringDocument] =  
      clClusterer.hierarchicalCluster(docSet)
      
    val partitionCount = EchoConfigAccessor.getInt("cluster.partitionCount")
    return dendrogram.partitionK(partitionCount)
    
  }
  
  def storeClusters(cluster: Set[Set[EchoClusteringDocument]]) = {
    
      EchoLogger.info("Storing the cluster to db")
      val clusterCollectionName = EchoConfigAccessor.getString("cluster.db.collectionName")
	  val collection : DBCollection = dataStore.getCollection(clusterCollectionName)
	  
	  for( set <- cluster.toArray[Set[EchoClusteringDocument]]( new Array[Set[EchoClusteringDocument]](0))){
	    val clusterId: UUID = UUID.randomUUID
	    EchoLogger.info("Creating a new cluster with id " + clusterId)
	    for( doc <- set.toArray[EchoClusteringDocument](new Array[EchoClusteringDocument](0))){
	      val id : UUID = doc.id
	      val set: BasicDBObject = new BasicDBObject("$set", new BasicDBObject("cluster", clusterId.toString))
	      val query : BasicDBObject = new BasicDBObject()
	      query.put("uuid", id.toString)
	      EchoLogger.info("Updating doc id " + id +  " with cluster id " + clusterId)
	      collection.update(query, set)
	    }
	  }
  }
   
  def receive = {
    case EchoMessage.EchoReClusterAll() => {
      EchoLogger.info("Received message for clustering")
      storeClusters(performClustering())
      EchoLogger.info("Clustering done! :)")
    }
    case unwantedMessages => {
      EchoLogger.fatal("Unwanted message " + unwantedMessages)
    }
  }
  
}