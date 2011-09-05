import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.*;
 
import java.io.IOException;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.*;


 
public class RunEchoServlet
{
    
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
 
        context.addServlet(new ServletHolder(new EchoServlet()),"/*");
        context.addServlet(new ServletHolder(new EchoServlet("open searchResults")),"/search/*");
        context.addServlet(new ServletHolder(new EchoServlet("open home page")),"/home/*");
 
        server.start();
        server.join();
    }
	
	static public class EchoServlet extends HttpServlet
	{
		private String get="Welcome to Echo!";
		public EchoServlet() { }
		public EchoServlet(String g)
		{
			get=g;
		}
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
		{
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			if(get=="open home page")
			response.getWriter().println("Echo Home");
			if(get=="open searchResults")
			response.getWriter().println("Search Results"+ request.getQueryString());
		
		

		}
	}
 


}
