package general

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http._;
import javax.servlet._;
 
import java.io.PrintWriter
import org.json.JSONObject

import servlet._

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler._;
import org.eclipse.jetty.server.nio._;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet._;



import akka.actor.Actor
import akka.dispatch.Future

import Actor._
import java.io.File
import utils._
import java.util.UUID
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import com.mongodb._



object EchoMain {
  
   val m = new Mongo("127.0.0.1")
  val dataStore = m.getDB("echo")
  val extracter = new EchoExtraction()
  
  val echo = actorOf(new EchoController())
  echo.start()
  
   
  def main( args: Array[String]){
    
       val server:Server = new Server(8080)
        val  context: ServletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        val holder1: ServletHolder= new ServletHolder(new RunEchoServlet("search",echo))
        val holder2: ServletHolder=new ServletHolder(new RunEchoServlet("rec",echo))
        context.addServlet(holder1,"/search/*");
        context.addServlet(holder2, "/rec/*")
        
        server.start();
        server.join();
        echo.stop();
    
   }
   /*// println("Echo testing starts")
    
    
    //add documents
    println("Echo Indexing starts")
    val dir : File = new File("C:\\Users\\kumarish\\Desktop\\book")
    dir.listFiles foreach ( file => {
            println("    Adding file " + file.getName())
    		echo ! EchoMessage.AddDocument( "C:\\Users\\kumarish\\Desktop\\book\\" + file.getName )
    	}
  	)
    
    println("Echo indexing finished")
    
    println("Start Clustering ?")
    var i = Console.readInt
    
    //do clustering
    echo ! EchoMessage.StartClustering
    
    //search query
    val query : String = Console.readLine
    val result = echo !! EchoMessage.Query(query)
 
    val res : Map[Float, UUID] = result.get.asInstanceOf[EchoMessage.QueryReply].result
    i = 0
    res.keySet foreach ( e => {
    		println(i + " : " + res.get(e).get + "  " + e)
    		i = i + 1
    	}
  	)
  
    //ask for recommendations for the books listed above
    
    
  }*/
  
}
