package networks1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;


class TCPServer implements Runnable { 
	
	public static final int SERVER_PORT = 80;
	public static int putIndex, postIndex, receive;
	
	/**
	 * Main method to run the server.
	 * For every socket that connects to the server, it will accept the socket and create a new TCPServer to handle it.
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception { 
		ServerSocket welcomeSocket = new ServerSocket(SERVER_PORT); 
		while(true) { 
			Socket clientSocket = welcomeSocket.accept(); 
			if(clientSocket != null){
				TCPServer server = new TCPServer(clientSocket);
				System.out.println("New Client");
				Thread t = new Thread(server);
				t.start();
			}
		}
	} 
	
	private Socket clientSocket;
	private BufferedReader inFromClient;
	private PrintWriter outToClient;
	
	/**
	 * Constructor of a new TCPServer. This server will respond to one client.
	 * This class is called ThreadHandler in the demo
	 * Takes a socket, and makes a BufferedReader for input and a PrintWriter for output.
	 * @param client
	 */
	public TCPServer(Socket client){
		try{
			clientSocket = client;
			inFromClient = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
			outToClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		} catch(Exception e){
			System.err.println("Error in TCPServer constructor.");
		}
		//System.out.println("We created the Server");
	}
	
	private boolean terminated = false;
	
	/**
	 * While not stopped, will read a line from the inputstreamn, and parse it.
	 * If an error occurs, it will shut down this client.
	 */
	public void run(){
		while(!terminated){
			if(clientSocket == null || !clientSocket.isConnected() || clientSocket.isClosed() || !clientSocket.isBound())
				terminated = true;
			else{
				try{
					String clientSentence = inFromClient.readLine(); 
					if(clientSentence != null){
						parse(clientSentence);
						System.out.println("Parsing " + clientSentence);
					}
				} 
				catch(SocketException e){
					terminated = true;
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		try{
			clientSocket.close();
		} catch(IOException ioe){
			System.out.println("Cannot close socket.");
		}
		System.out.println("Shutting down ThreadHandler.");
	}
	
	private int version;
	
	/**
	 * Parses the command, checks the first word and gives the command to the correct method.
	 * @param parsing
	 */
	private void parse(String parsing){
		if(parsing.toLowerCase().startsWith("get")){
			get(parsing);
		} else if (parsing.toLowerCase().startsWith("head")){
			head(parsing);
		} else if (parsing.toLowerCase().startsWith("put")){
			put(parsing);
		} else if (parsing.toLowerCase().startsWith("post")){
			post(parsing);
		}
	}
	
	/**
	 * Parses the get command.
	 * Parses the first line to check for the file and the version.
	 * Then it will check the header.
	 * Next it will search the file and prepare to send it back.
	 * At last, it fetches our header, put the needed body behind it and send it back.
	 * If an error occurs, it will be reported accordingly.
	 * @param command	The command given by the client with get as first word.
	 */
	private void get(String command){
		String toPrint = "";
		String output = "";
		String error = "200 OK";
		String[] commands = command.split("[ ]+");
		if(!(commands.length == 3 || commands.length == 2)){ //If a bad command is given, we send back an error.
			error = "400 Bad request";
			output = "<html><body>400 Bad request. Wrong amount of commands.</html></body>";
		} else{
			if(commands.length == 3){
				String versionString = commands[2];
				if(versionString.equals("HTTP/1.0"))
					version = 0;
				else 
					version = 1;
			} else
				version = 1;
			String file = commands[1];
			if(file.equals("/"))
				file = "/index.html";
			
			if(!checkHeader()){
				output = "<html><body>400 Bad request. Header did not match</html></body>";
				error = "400 Bad request";
			} else{
				File realFile = new File("src/networks1/webpages" + file);
				try{
					BufferedReader fileReader = new BufferedReader(new FileReader(realFile));
					String next = fileReader.readLine();
					while(next != null){					
						output = output + next;
						next = fileReader.readLine();
					}
					fileReader.close();
				} catch(FileNotFoundException fnfe){
					error = "404 Not found.";
					output = "<html><body>Error 404, file not found.<body></html>";
				}
				catch(IOException ioe) {
					System.err.println("Error in sending messages back. @ get @ TCPServer");
					error = "500 Internal server error";
					output = "<html><body>Error 500 Internal server error. Cannot make fileReader</body></html>";
				}
			}
		}
		toPrint = getHeader(error);
		toPrint += "\n" + output;
		System.out.println(error);
		sendBack(toPrint);
		if(version == 0)
			terminated = true;
	}
	
	/**
	 * Parses the head command.
	 * Parses the first line to check for the file and the version.
	 * Then it will check the header.
	 * At last, it fetches our header, put the needed body behind it and send it back.
	 * @param command	The command given by the client with get as first word.
	 */
	private void head(String command){
		String toPrint = "";
		String error;
		String output = "";
		String[] commands = command.split("[ ]+");
		if(!(commands.length == 3 || commands.length == 2)){ //If a bad command is given, we send back an error.
			error = "400 Bad request";
			output = "<html><body>400 Bad request. Wrong amount of commands.</html></body>";
		} else{
			if(commands.length == 3){
				String versionString = commands[2];
				if(versionString.equals("HTTP/1.0"))
					version = 0;
				else 
					version = 1;
			} else
				version = 1;
			
			if(!checkHeader())
				error = "400 Bad request";
			else
				error = "200 OK";
			toPrint = getHeader(error);
			toPrint += output;
			System.out.println(error);
			sendBack(toPrint);
			if(version == 0)
				terminated = true;
		}
	}
	
	/**
	 * Parses the put command.
	 * @effect post(command)
	 */
	private void put(String command){
		post(command);
	}
	
	/**
	 * Parses the post command.
	 * Parses the first line to check for the file and the version.
	 * Then it will check the header.
	 * It will parse the inputstream until it ends. It will save the incoming files to a .txt file on the computer.
	 * At last, it fetches our header, put a confirmation or error code behind it and send it back.
	 * If an error occurs, it will be reported accordingly.
	 * @param command	The command given by the client with post as first word.
	 */
	private void post(String command){
		String toPrint = "";
		String output = "";
		String error = "";
		String[] commands = command.split("[ ]+");
		if(!(commands.length == 3 || commands.length == 2)){ //If a bad command is given, we send back an error.
			error = "400 Bad request";
			output = "<html><body>400 Bad request. Wrong amount of commands.</html></body>";
		} else{
			if(commands.length == 3){
				String versionString = commands[2];
				if(versionString.equals("HTTP/1.0"))
					version = 0;
				else 
					version = 1;
			} else
				version = 1;
			
			if(!checkHeader()){
				toPrint = getHeader("400 Bad request");
				output = "<html><body>400 Bad request. Header did not match</html></body>";
			} else{			
				try{
					OutputStream toFile = new FileOutputStream("src/networks1/post/post" + postIndex + ".txt");
					while (((output = inFromClient.readLine()) != null) && inFromClient.ready()) {
						//System.out.println(output);
						toFile.write(output.getBytes());
						toFile.write("\n".getBytes());
					}
					toFile.close();
					error = "200 OK";
					output = "<html><body>Read post. Thank you.</body></html>";
				} catch(Exception e){
					System.err.println("Error in post. Cannot write data to system.");
					error = "500 Internal server error";
					output = "<html><body>500 Internal server error. Could not write to server. Please try again. </body><html>";
				}
			}
		}
		toPrint = getHeader(error);
		toPrint += output;
		System.out.println(error);
		sendBack(toPrint);
		if(version == 0)
			terminated = true;
	}
	
	/**
	 * Sends back the string provided.
	 * @param toPrint	The string to return to the client.
	 */
	private void sendBack(String toPrint){
		if(!toPrint.equals("")){
			try{
				outToClient.println(toPrint);
				outToClient.println();
				outToClient.flush();
			} catch(Exception e){
				System.err.println("Error in sendBack @ TCPServer");
			}
		}
	}
	
	/**
	 * Returns the header. He needs the HTTP status code to set in the header.
	 * @param status	The statuscode and explanation of the status to return.
	 * @return			The complete header to return to the client.
	 */
	private String getHeader(String status){
		String header =  "HTTP/1." + version + " " +status + "\n";
		header += "Server: Localhost\n";
		header += "Content-language: nl\n";
		header += "Last-Modified: Wed, 2 Sep 2093 13:37:00 CET\n";
		header += "Date: " + (new Date()).toString() +"\n";
		if(version == 1)
			header += "Connection: keep-alive\n";
		else 
			header += "\n";
		return header;
	}
	
	/**
	 * Checks the next line for the header. He checks if there is a line with "Host: localhost:" PORT.
	 * If the port is 80, it is not required to specify the port.
	 * He will check all of the header and will stop at the first empty line.
	 * @return	If the header is present. 
	 * 			With HTTP 1.0, the header is not required but if a header is present, it has to have a correct header.
	 */
	private boolean checkHeader(){
		String nextLine = "";
		boolean realHeader = true;
		boolean returnBool = false;
		try{
			nextLine = inFromClient.readLine();
			if(nextLine.isEmpty() && version == 0) // If there is no header present and the version is HTTP/1.0 then it is accepted.
				returnBool = true;
			while(nextLine != null && !(nextLine).isEmpty() && inFromClient.ready()){
				if(realHeader){
					String correct = "Host: localhost";
					if(SERVER_PORT != 80) //If we would change the port.
						correct += ":" + SERVER_PORT;
					if(nextLine.equals(correct) || nextLine.equals(correct + ":" + SERVER_PORT)) //If there is a header, then the correct syntax must be found at least once.
						returnBool = true;;
				}
				nextLine = inFromClient.readLine();
			}
		} catch(IOException ioe){
			System.err.println("Error in checkHeader. Cannot read next line from client.");
			return false;
		}
		return returnBool;
	}
	
	public static void increasePutIndex(){
		putIndex++;
	}
	public static void increasePostIndex(){
		postIndex++;
	}
	public static void increaseReceiveIndex(){
		receive++;
	}
} 