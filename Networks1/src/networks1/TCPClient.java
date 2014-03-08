package networks1;

import java.io.*; 
import java.net.*; 


class TCPClient { 
	
	//#DIT IS MARTIN FROM GITHUB
	
//	public static void main(String[] args) {
//	    try {
//	      // Check the arguments
//	      if ((args.length != 1) && (args.length != 2))
//	        throw new IllegalArgumentException("Wrong number of arguments");
//	      
//	      // Get an output stream to write the URL contents to
//	      OutputStream to_file;
//	      if (args.length == 2) to_file = new FileOutputStream(args[1]);
//	      else to_file = System.out;
//	      
//	      // Now use the URL class to parse the user-specified URL into
//	      // its various parts: protocol, host, port, filename.  Check the protocol
//	      URL url = new URL(args[0]);
//	      String protocol = url.getProtocol();
//	      if (!protocol.equals("http"))
//	        throw new IllegalArgumentException("URL must use 'http:' protocol");
//	      String host = url.getHost();
//	      int port = url.getPort();
//	      if (port == -1) port = 80;  // if no port, use the default HTTP port
//	      String filename = url.getFile();
//	      // Open a network socket connection to the specified host and port
//	      Socket socket = new Socket(host, port);
//	      // Get input and output streams for the socket
//	      InputStream from_server = socket.getInputStream();
//	      PrintWriter to_server = 
//	        new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
//	      
//	      // Send the HTTP GET command to the Web server, specifying the file.
//	      // This uses an old and very simple version of the HTTP protocol
//	      to_server.println("GET " + filename);
//	      to_server.flush();  // Send it right now!
//	      
//	      // Now read the server's response, and write it to the file
//	      byte[] buffer = new byte[4096];
//	      int bytes_read;
//	      while((bytes_read = from_server.read(buffer)) != -1)
//	        to_file.write(buffer, 0, bytes_read);
//	      
//	      // When the server closes the connection, we close our stuff
//	      socket.close();
//	      to_file.close();
//	    }
//	    catch (Exception e) {    // Report any errors that arise
//	      System.err.println(e);
//	      System.err.println("Usage: java HttpClient <URL> [<filename>]");
//	    }
//	  }
	
	public static void main(String argv[]) throws Exception { 
		BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in)); 
		String sentence = inFromUser.readLine();
		while(true){
			parse(sentence);
			sentence = inFromUser.readLine();
		}
	} 
	// GET http://www.google.com/index.html 80
	// GET http://www.example.com/index.html 80 HTTP/1.1
	private Socket clientSocket;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	
	public TCPClient(String host, int port) throws Exception{
		
		if(host == null || host =="" || port < 0)
			throw new IllegalArgumentException("Please specify a host and a port.");
		try {
			clientSocket = new Socket(host, port);
			System.out.println("Connected to: " + host + " with port: " + port + ".");
			outToServer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void parse(String sentence) throws Exception{
		String[] commands = sentence.split("[ ]+");
		if(!(commands.length == 4 || commands.length == 3))
			throw new IllegalArgumentException("FUCK YOU");
		String command = commands[0];
		URL url = new URL(commands[1]);
		int port = Integer.parseInt(commands[2]);
		String version = "";
		try{
			version = commands[3];
		} catch (Exception e){}
		
		TCPClient client = new TCPClient(url.getHost(), port);
		client.sendMessage(command, url, version, port);
		
		client.closeConnection();
	}
	
	private void closeConnection(){
		try{
			clientSocket.close();
			System.out.println("Closing Socket");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String command, URL url, String version, int port){
		try{
			String sentence = command + " " + url.getFile() + " " + version;
			if(version.equals("HTTP/1.1"))
				sentence += "\nHost: "+ url.getHost() + ":" + port +"\n";
			System.out.println("Sending: " +sentence);
			outToServer.println(sentence);
			
			outToServer.flush();
			System.out.println("Flushed the writer.");
			String modifiedSentence = inFromServer.readLine(); 
			while(modifiedSentence != null){
				System.out.println("FROM SERVER: " + modifiedSentence);
				modifiedSentence = inFromServer.readLine();
			}
			System.out.println("Done with receiving lines.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
} 
