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
	
	public static final int SERVER_PORT = 6789;
	public static int putIndex, postIndex, receive;
	
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
					if(clientSentence != null)
						System.out.println("Parsing " + clientSentence);
					if(clientSentence != null)
						parse(clientSentence);
				} 
				catch(SocketException e){
					//terminated = true;
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
			
			toPrint += getHeader() + "\n";
			System.out.println(file);
			File realFile = new File("src/networks1/webpages" + file);
			System.out.println(realFile);
			try{
				BufferedReader fileReader = new BufferedReader(new FileReader(realFile));
				String next = fileReader.readLine();
				while(next != null){					
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
	
	private void put(String command){
		System.out.println("WE GOT TO PUT!");
		post(command);
	}
	
	private void post(String command){
		System.out.println("WE GOT TO POST!");
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
			
			try{
				OutputStream toFile = new FileOutputStream("src/networks1/post/post" + postIndex + ".txt");
				//PrintWriter pw = new PrintWriter("src/networks1/post/" + postIndex, "UTF-8");
				String modifiedSentence = inFromClient.readLine(); 
				while(modifiedSentence != null){
					System.out.println(modifiedSentence);
					toFile.write(modifiedSentence.getBytes());
					modifiedSentence = inFromClient.readLine();
				}
				toFile.close();
			} catch(Exception e){
				System.err.println("Error in post. Cannot write data to system.");
			}
			toPrint = "Read post. Thank you.";
			sendBack(toPrint);
			if(version == 0)
				terminated = true;
		}
	}
	
	private void sendBack(String toPrint){
		if(!toPrint.equals("")){
			try{
				outToClient.println(toPrint);
				outToClient.flush();
				System.out.println("Sent: \n" + toPrint);
				//outToClient.close();
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
		String correct = "Host: localhost:" + SERVER_PORT;
		if(version == 0){
			if(header == null || header == "" || header.isEmpty() || header.equals(correct)){
				return true; }
		} else{ //version == 1
			if(header.equals(correct))
				return true;
			return false;
		}
		return false;
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