* handler - like a servlet but more powerful
* anything that handles HTTP requests is derived from a handler.
* many types of handlers
		* AbstractHandler
		* ResourceHandler
		* DefaultHandler etc.
		* for a web application: ContextHandler, SessionHandler, WebAppContext  are important
* public void handle ( String target, Request baseRequest => jetty api request  that is mutable , HTTPServletRequest request => immutable , HTTPServletResponse response => response from the HTTP request)
* useful fns for an obj response of type HttpServletResponse 
	* response.setContentType("text/html")
	* response.setStatus(HttpServletResponse.SC_OK)
	* response.getWriter().println("Some html tag goes here");
* To start the server, in the mail function, we need to:
	* make a new server object( the Server class is imported from org.eclipse.jerry.server; It has a class called Handler also) and set the port 
		-> Server server= new Server(8080)
	* set the handler using your class thats going to handle your HTTP requests 
		-> server.setHandler(handlerName)
	* Start your server:
		-> server.start()
	* join your server which means we wait until the requests are handled 
		-> server.join()
* there are other handlers also:
	* there are some channelconnectors. this is how you use them:
			*  SelectChannelConnector conn= new SelectChannelConnector()
			*set the port => conn.setPort(8080)
			*add to the server => server.addConnector(conn)
			
  * resource handler: some useful fns:
		* setDirectoriesListed(boolean )
		* setWelcomeFiles( files as strings)
		* setResourceBase(" directory to look in, '.' => pwd")
* you can set handlers to multiple handlers at the same time this way:
		* HandlerList handlers = new HandlerList();
		* add handlers to your server using setHandler()

		* 
		
	
