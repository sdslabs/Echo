package servlet

import general._
import utils._

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


class EchoServlet(actor : ActorRef) extends HttpServlet
	{
     
     
	   val m = new Mongo("127.0.0.1")
	   	val dataStore = m.getDB("echo")
	   	val extracter = new EchoExtraction()
  
		var query:String ="Welcome to Echo!";
		//getInfo() given the id of the book
		
					def getInfo(id : UUID) : Map[String,String] = {
   					val query : BasicDBObject = new BasicDBObject()
					query.put("id", id.toString)
					val coll = dataStore.getCollection("echo")
					val cur = coll.find(query)
					var res : Map[String,String] = new HashMap[String,String]()
    
					while(cur.hasNext){
						res = extracter.format(cur.next)
							}
						return res 
    
						}
		//end of getInfo()
		
		
		
		
		
		
		
		
		override def doGet(request: HttpServletRequest , response:HttpServletResponse )
		{
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			//Taking in the search query , passing to EchoController , handling results. 
			             query=request.getQueryString()
                         val result = actor ? EchoMessage.Query(query) onTimeout() {
			               
			               response.getWriter().println("Your Search Request has timed out!")
			               
			             }
                        val res : Map[Float, UUID] = result.get.asInstanceOf[EchoMessage.QueryReply].result   
                        val searchResults: List[Map[String,String]]= null 
                        // adding to the List the information for each book
                        
                          
                                  res.keySet foreach ( e => { 
    		                    searchResults:::getInfo(res.get(e))
    		                    
    		                      })
                        // returning searchResults as a JSON object
                        
                        val sResults:JSONObject = new JSONObject(searchResults)
                        response.setContentType("application/json");
    		                      	// Get the printwriter object from response to write the required json object to the output stream      
    		                      	val out: PrintWriter = response.getWriter();
    		                      	// Assuming your json object is **jsonObject**, perform the following, it will return your json object  
    		                      	out.print(sResults);
    		                      	out.flush();
                        
  
			            
			
		

		}
	}
 




