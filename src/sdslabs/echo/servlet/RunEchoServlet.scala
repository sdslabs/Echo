package sdslabs.echo.servlet

import sdslabs.echo.general._
import sdslabs.echo.utils._

import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import com.mongodb._


import akka.actor._
import akka.actor.Actor._
import akka.actor.Actor
import akka.util.duration._

import java.util.UUID
import java.util._ 
import java.io.PrintWriter
import org.json.JSONObject

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http._;
import javax.servlet._;
 
import java.io.IOException;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler._;
import org.eclipse.jetty.server.nio._;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet._;


class RunEchoServlet(wtd: String,actor : ActorRef) extends HttpServlet{
 
   val ipAdd = EchoConfigAccessor.getString("echo.db.ipAdd")
   val m = new Mongo(ipAdd)
   val dbName = EchoConfigAccessor.getString("echo.db")
   val dataStore = m.getDB(dbName)
   val extracter = new EchoExtraction()
  
		
   //getInfo() given the id of the book
	
   def getInfo(id : UUID) : Map[String,String] = {
	   val query : BasicDBObject = new BasicDBObject()
	   query.put("id", id.toString)
	   val collectionName = EchoConfigAccessor.getString("echo.db.collectionName")
	   val coll = dataStore.getCollection(collectionName)
	   val cur = coll.find(query)
	   var res : Map[String,String] = new HashMap[String,String]()
	
	   while(cur.hasNext){
		   res = extracter.format(cur.next)
	   }
	   return res 

   }
	
   //end of getInfo()
   override def doGet(request: HttpServletRequest , response:HttpServletResponse ){
     response.setContentType("text/html");
     response.setStatus(HttpServletResponse.SC_OK);
     //Taking in the search query , passing to EchoController , handling results. 
         
     if(wtd=="search"){
       var query=request.getParameter("query")
       val result = actor !!! EchoMessage.Query(query)
       
       val res : Map[Float, UUID] = result.get.asInstanceOf[EchoMessage.QueryReply].result   
       val searchResults: List[Map[String,String]] = new ArrayList[Map[String,String]]() 
       // adding to the List the information for each book
       res.keySet foreach ( e => { 
    	   searchResults.add(getInfo(res(e)))
       })
       
       // returning searchResults as a JSON object

       val sResults:JSONObject = new JSONObject(searchResults.get(0))
       response.setContentType("application/json");
       //Get the printwriter object from response to write the required json object to the output stream      
       val out: PrintWriter = response.getWriter();
       //performing the following returns the json object  
       out.print(sResults);
       out.flush();
        
     }
  
     if(wtd=="rec"){
       val bid:String = request.getParameter("bid")
       val res = actor !!! EchoMessage.GetRecommendation(bid) 
	   val recResults : List[Map[String,String]]= res.get.asInstanceOf[EchoMessage.RecommenderReply].res.asInstanceOf[List[Map[String,String]]]
	   // returning recommendation results as a JSONObject
       val rResults: JSONObject= new JSONObject(recResults)
       response.setContentType("application/json")
       //Get the printwriter object from response to write the required json object to the output stream
       val out: PrintWriter = response.getWriter();
       //  performing the following returns the json object
       out.print(rResults);
       out.flush();
	 }
   }
}

