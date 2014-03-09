package networks1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.LinkedList;


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
	
	/**
	 * Main method. Creates a reader, reads a sentence.
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception { 
		BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in)); 
		String sentence = inFromUser.readLine();
		while(true){
			parse(sentence);
			sentence = inFromUser.readLine();
		}
	} 
	
	/**
	 * Parse a sentence: split it, check if correct syntax. When correct, a Client is created, to send and receive messages.
	 * @param sentence
	 * @throws Exception
	 */
	private static void parse(String sentence) throws Exception{
		String[] commands = sentence.split("[ ]+");
		if(!(commands.length == 4))
			System.err.println("Please enter a correct statement. \nThe correct syntax is: HTTPCommand URI Port HTTPversion");
		else{
			//1- command
			String command = commands[0];
			//2- url
			URL url;
			if(commands[1].toLowerCase().startsWith("http://"))
				url = new URL(commands[1]);
			else
				url = new URL("http://" + commands[1]);
			//3- port
			int port = Integer.parseInt(commands[2]);
			//4- version
			String version;
			if(commands[3].toLowerCase().startsWith("http/"))
				version = commands[3];
			else
				version = "HTTP/"+commands[3];	
			
			TCPClient client = new TCPClient(command, url, port, version);
			client.sendMessage(command, url, version, port);
			client.receiveMessage();
			if(version.equals("HTTP/1.0"))
				client.closeConnection();
		}
	}
	
	// GET www.google.com/index.html 80
	// GET www.example.com/index.html 80 HTTP/1.1
	// GET www.travian.nl/ 80 HTTP/1.1
	// GET nl.wikipedia.org/wiki/Hoofdpagina 80 HTTP/1.1
	// GET http://www.student.kuleuven.be/~r0299122/DOCUMENTEN/zitting3.html 80 HTTP/1.0
	// GET localhost/index.html 6789 HTTP/1.0
	private Socket clientSocket;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private String command, version;
	private URL url;
	private int port;
	
	/**
	 * Client constructor. Opens a Socket, en listens to the server.
	 * @param command
	 * @param url
	 * @param port
	 * @param version
	 * @throws Exception
	 */
	public TCPClient(String command, URL url, int port, String version) throws Exception{
		
		if(url == null || url.getHost() =="" || port < 0)
			throw new IllegalArgumentException("Please specify a host and a port.");
		this.command = command;
		this.url = url;
		this.port = port;
		this.version = version;
		imagesNeeded = new LinkedList<String>();
		try {
			clientSocket = new Socket(url.getHost(), port);
			System.out.println("Connected to: " + url.getHost() + " with port: " + port + ".");
			outToServer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
	
	/**
	 * Send the correct message to the server.
	 * @param command
	 * @param url
	 * @param version
	 * @param port
	 */
	public void sendMessage(String command, URL url, String version, int port){
		try{
			String sentence = command + " " + url.getFile() + " " + version;
			if(version.equals("HTTP/1.1"))
				sentence += "\nHost: "+ url.getHost() + ":" + port;
			sentence += "\n";
			System.out.println("Sending: " +sentence);
			outToServer.println(sentence);
			
			outToServer.flush();
			System.out.println("Flushed the writer.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Receive and print what the server sends. Check if there are images.
	 */
	public void receiveMessage(){
		try{
			String modifiedSentence = inFromServer.readLine(); 
			while(modifiedSentence != null){
				searchForImages(modifiedSentence);
				System.out.println(modifiedSentence);
				modifiedSentence = inFromServer.readLine();
			}
			System.out.println("Done with receiving code lines.");
			if(version.equals("HTTP/1.0")){					
				for(String imageNeeded: imagesNeeded)
					retrieveImage(imageNeeded);
			}
		}
		catch(SocketException ses){
			System.out.println("Socket close by server. Please try again.");
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	private LinkedList<String> imagesNeeded;
	
	/**
	 * Search if a sentence contains an image source.
	 * @param sentence
	 */
	private void searchForImages(String sentence){
		if(sentence.toLowerCase().contains("<img")){
			int src = sentence.indexOf("src=");
			int begin = sentence.indexOf('"', src) + 1;
			int end = sentence.indexOf('"', begin);
			if(version.equals("HTTP/1.0"))
				imagesNeeded.add(sentence.substring(begin, end));
			else if(version.equals("HTTP/1.1")){
				ImageGetter ig = new ImageGetter(clientSocket, url, port, sentence.substring(begin, end));
				(new Thread(ig)).start();
			}
		}
	}
	
	/**
	 * Retrieve images for HTTP/1.0.
	 * @param imageNeeded
	 */
	private void retrieveImage(String imageNeeded){
		if(imageNeeded.toLowerCase().startsWith("http://") || imageNeeded.toLowerCase().startsWith("www.")){ //full address
			outToServer.println("GET " + imageNeeded + " " + port + " " + version);
			System.out.println("GET " + imageNeeded + " " + port + " " + version);
		}
		else{ //relative address
			outToServer.println("GET " + url.getHost() + imageNeeded + " " + port + " " + version);
			System.out.println("GET " + imageNeeded + " " + port + " " + version);
		}
		outToServer.println("Host: " + url.getHost() + ":" + port);
		outToServer.println();
		try{
			String modifiedSentence = inFromServer.readLine(); 
			while(modifiedSentence != null){
				searchForImages(modifiedSentence);
				System.out.println("FROM SERVER: " + modifiedSentence);
				modifiedSentence = inFromServer.readLine();
			}
			System.out.println("Done with receiving image.");
		} catch(Exception e){
			e.printStackTrace();
		}
	}
} 
