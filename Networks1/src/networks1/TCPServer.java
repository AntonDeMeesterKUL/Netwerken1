package networks1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;


class TCPServer implements Runnable { 
	
	public static final int SERVER_PORT = 6789;
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
	
	public TCPServer(Socket client){
		try{
			clientSocket = client;
			inFromClient = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
			outToClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		} catch(Exception e){
			System.err.println("Error in TCPServer constructor.");
		}
		System.out.println("We created the Server");
	}
	
	private boolean terminated = false;
	
	public void run(){
		while(!terminated){
			if(clientSocket == null || !clientSocket.isConnected() || clientSocket.isClosed() || !clientSocket.isBound())
				terminated = true;
			else{
				try{
					String clientSentence = inFromClient.readLine(); 
					System.out.println("Parsing " + clientSentence);
					if(clientSentence != null)
						parse(clientSentence);
				} 
				catch(SocketException e){
					terminated = true;
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		System.out.println("Shutting down ThreadHandler.");
	}
	
	private int version;
	
	private void parse(String parsing){
		if(parsing.toLowerCase().startsWith("get")){
			get(parsing);
		} else if (parsing.toLowerCase().startsWith("head")){
			head(parsing);
		} else if (parsing.toLowerCase().startsWith("put")){
			put(parsing);
		} else if (parsing.toLowerCase().startsWith("post")){
			post(parsing);
		} else
			;//FUCK YOU
	}
	
	private void get(String command){
		System.out.println("We got to GET");
		String[] commands = command.split("[ ]+");
		if(!(commands.length == 3 || commands.length == 2))
			return;
		else{
			if(commands.length == 3){
				String versionString = commands[2];
				if(versionString.equals("HTTP/1.0"))
					version = 0;
				else 
					version = 1;
			} else
				version = 1;
			String file = commands[1];
			String toPrint = "";
			
			if(!checkHeader())
				return;
			
			File realFile = new File("/webpages" + file);
			try{
				BufferedReader fileReader = new BufferedReader(new FileReader(realFile));
				String next = fileReader.readLine();
				while(next != null){
					toPrint += getHeader() + "\n";
					toPrint = toPrint + next + "\n";
					next = fileReader.readLine();
				}
				fileReader.close();
			} catch(FileNotFoundException fnfe){
				toPrint = "Error 404, file not found. Please try another page.";
			}
			catch(IOException ioe) {
				System.err.println("Error in sending messages back. @ get @ TCPServer");
			}
			sendBack(toPrint);
			if(version == 0)
				terminated = true;
		}
	}
	
	private void head(String command){
		System.out.println("WE GOT TO HEAD!");
		String[] commands = command.split("[ ]+");
		if(!(commands.length == 3 || commands.length == 2))
			return;
		else{
			if(commands.length == 3){
				String versionString = commands[2];
				if(versionString.equals("HTTP/1.0"))
					version = 0;
				else 
					version = 1;
			} else
				version = 1;
			String toPrint = "";
			if(!checkHeader())
				return;
			
			toPrint = getHeader();
			sendBack(toPrint);
			if(version == 0)
				terminated = true;
		}
	}
	
	private void put(String commands){
		post(commands);
	}
	
	private void post(String commands){
	}
	
	private void sendBack(String toPrint){
		if(!toPrint.equals("")){
			try{
				outToClient.println(toPrint);
				outToClient.flush();
				System.out.println(toPrint);
			} catch(Exception e){
				System.err.println("Error in sendBack @ TCPServer");
			}
		}
	}
	
	private String getHeader(){
		String header =  "HTTP/1." + version + " 200 OK\n";
		header += "Server: Localhost\n";
		header += "Content-language: nl\n";
		header += "Last-Modified: Wed, 2 Sep 2093 13:37:00 CET\n";
		header += "Date: " + (new Date()).toString();
		if(version == 1)
			header += "Connection: keep-alive\n";
		return header;
	}
	
	private boolean checkHeader(){	
		String header = "";
		try{
			header = inFromClient.readLine();
			System.out.println("header is: " + header);
		} catch(IOException ioe){
			System.err.println("Error in checkHeader. Cannot read next line from client.");
			return false;
		}
		if(version == 0){
			if(header == null || header == "" || header.isEmpty()){
				return true; }
		} else{ //version == 1
			String correct = "Host: localhost:" + SERVER_PORT;
			if(header.equals(correct))
				return true;
			return false;
		}
		return false;
	}
} 