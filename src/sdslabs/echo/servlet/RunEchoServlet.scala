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

   
		
     override def doGet(request: HttpServletRequest , response:HttpServletResponse ){
     response.setContentType("application/json");
     response.setStatus(HttpServletResponse.SC_OK);
     //Taking in the search query , passing to EchoController , handling results. 
         
     if(wtd=="search"){
       var query=request.getParameter("query")
       val result = actor !!! EchoMessage.Query(query)
       
       val res : Map[Float, UUID] = result.get.asInstanceOf[EchoMessage.QueryReply].result   
       val sRWR = new ArrayList()
        res.foreach(book => sRWR.add(book["id"]))  
       
       
              val sResults:JSONObject = new JSONObject(sRWR)
       
       //Get the printwriter object from response to write the required json object to the output stream      
       val out: PrintWriter = response.getWriter();
       //performing the following returns the json object  
       out.print(sResults);
       out.flush();
      

     }
    
}

