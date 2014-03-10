package networks1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

public class Client {
	
	// GET www.google.com/index.html 80
	// GET www.example.com/index.html 80 HTTP/1.1
	// GET www.travian.nl/ 80 HTTP/1.1
	// GET nl.wikipedia.org/wiki/Hoofdpagina 80 HTTP/1.1
	// GET http://www.student.kuleuven.be/~r0299122/DOCUMENTEN/zitting3.html 80 HTTP/1.0
	// GET localhost/index.html 6789 HTTP/1.0
	
	/**
	 * Main method. Reads lines, calls Parse-method and creates a Client with the appropriate commands.
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception {
		boolean expectCommand = true;
		boolean correctSyntax = true;
		String messageBody = "";
		HashMap<String, Client> clients = new HashMap<String,Client>();
		String[] parsed = new String[4];
		BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in)); 
		while(true){
			String sentence = inFromUser.readLine();
			if(expectCommand){ //we expect a command, not a message body
				try{
					parsed = parseCommand(sentence);
					if(parsed[0].equals("POST") || parsed[0].equals("PUT")){
						expectCommand=false;
						messageBody="";
					}
					else
						expectCommand=true;
					correctSyntax=true;
				}
				catch(Exception e){
					System.err.println("Please enter a correct statement. \nThe correct syntax is: HTTPCommand URI Port HTTPversion. \nRecognised HTTPCommands are 'GET', 'HEAD', 'PUT' and 'POST'.");
					correctSyntax = false;
				}
			}
			else{ //we expect a message body, not a command
				if(sentence.isEmpty()){
					expectCommand=true;
				}
				else
					messageBody += "\n" + sentence;
			}
			if(expectCommand && correctSyntax){ //previous command is finished, so we can send it
				URL url = new URL(parsed[1]);
				if(clients.containsKey(url.getHost()) && parsed[3].equals("HTTP/1.1")) //we already have a client to this host with HTTP/1.1
					clients.get(url.getHost()).sendMessage(parsed[0], url, Integer.parseInt(parsed[2]), parsed[3], messageBody);
				else if (parsed[3].equals("HTTP/1.1")) //store new client in hashmap, for future connections to the same host
					clients.put(url.getHost(), new Client(parsed[0], url, Integer.parseInt(parsed[2]), parsed[3], messageBody));
				else //http/1.0
					new Client(parsed[0], url, Integer.parseInt(parsed[2]), parsed[3], messageBody);
			}
		}
	}
	
	/**
	 * Reads a given string and checks if it matches the HTTP-command syntax.
	 * @param sentence
	 * @return
	 * @throws Exception
	 */
	private static String[] parseCommand(String sentence) throws Exception{
		String[] split = sentence.split("[ ]+");
		String[] command = new String[4];
		if(!(split.length == 4))
			throw new IllegalArgumentException();
		else{
			//1- command
			command[0] = split[0];
			if(!(command[0].equals("GET") || command[0].equals("HEAD") || command[0].equals("PUT") || command[0].equals("POST")))
				throw new IllegalArgumentException();
			//2- url
			if(split[1].toLowerCase().startsWith("http://"))
				command[1] = split[1];
			else
				command[1] = "http://" + split[1];
			//3- port
				command[2] = "" + Integer.parseInt(split[2]);
			//4- version
			if(split[3].toLowerCase().startsWith("http/"))
				command[3] = split[3];
			else
				command[3] = "HTTP/"+split[3];	
			return command;
		}
	}
	
	private Socket clientSocket;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private String version;
	private URL url;
	private int port;
	private LinkedList<String> imagesNeeded;
	
	/**
	 * Client constructor. Creates a socket to the given host, creates a reader and writer for the server. Calls send/receive/close methods.
	 * @param command
	 * @param url
	 * @param port
	 * @param version
	 * @param messageBody
	 * @throws Exception
	 */
	public Client(String command, URL url, int port, String version, String messageBody) throws Exception{
		if(url == null || url.getHost() =="" || port < 0)
			throw new IllegalArgumentException("Please specify a host and a port.");
		this.url = url;
		this.port = port;
		this.version = version;
		imagesNeeded = new LinkedList<String>();
		try {
			createConnection(url.getHost(), port);
			System.out.println("Connected to: " + url.getHost() + " with port: " + port + ".");
			outToServer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		sendMessage(command, url, port, version, messageBody);
		if(version.equals("HTTP/1.0"))
			closeConnection();
	}
	
	/**
	 * Open a socket.
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	private void createConnection(String host, int port) throws Exception{
		try {
			clientSocket = new Socket(host, port);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Close a socket.
	 */
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
	 * Send a message to the server, with the correct syntax.
	 * @param command
	 * @param url
	 * @param version
	 * @param port
	 * @param messageBody
	 */
	public void sendMessage(String command, URL url, int port, String version, String messageBody){
		try{
			String message = command + " " + url.getFile() + " " + version;
			if(version.equals("HTTP/1.1")) //mandatory Host-header
				message += "\nHost: "+ url.getHost() + ":" + port;
			if(command.equals("PUT") || command.equals("POST")) 
				message += messageBody;
			else
				message += "\n";
			System.out.println("Sending: " + message);
			outToServer.println(message);
			outToServer.flush();
			System.out.println("Flushed the writer.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		receiveMessage();
	}
	
	/**
	 * Receive and print a message given by the server.
	 */
	private void receiveMessage(){
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
		} catch(SocketException ses){
			System.out.println("Socket close by server. Please try again.");
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
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
