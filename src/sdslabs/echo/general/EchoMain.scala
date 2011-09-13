package sdslabs.echo.general

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException
import javax.servlet.http._
import javax.servlet._
import java.io.PrintWriter
import org.json.JSONObject
import sdslabs.echo.servlet.RunEchoServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.handler._
import org.eclipse.jetty.server.nio._
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.servlet._
import akka.actor.Actor
import akka.dispatch.Future
import Actor._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import scala.collection.JavaConversions.asScalaMap
import sdslabs.echo.utils.EchoConfigAccessor


object EchoMain {

  val echo = actorOf(new EchoController())
  echo.start()
    
  def main( args: Array[String]){
 
    val port = EchoConfigAccessor.getInt("echo.server.port")
    val server:Server = new Server(port)
    val  context: ServletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
	server.setHandler(context)
	val holder1: ServletHolder= new ServletHolder(new RunEchoServlet("search",echo))
	val holder2: ServletHolder=new ServletHolder(new RunEchoServlet("rec",echo))
	context.addServlet(holder1,"/search/*")
	context.addServlet(holder2, "/rec/*")
	
	server.start()
	server.join()
	echo.stop()    
  }
  
}
