package networks1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


class TCPServer implements Runnable { 
	
	public static void main(String argv[]) throws Exception { 
		ServerSocket welcomeSocket = new ServerSocket(6789); 
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
	private DataOutputStream outToClient;
	
	public TCPServer(Socket client){
		try{
			clientSocket = client;
			inFromClient = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
			outToClient = new DataOutputStream(clientSocket.getOutputStream()); 
		} catch(Exception e){
			System.err.println("Error in TCPServer constructor.");
		}
		totalCommand = new LinkedList<String>();
	}
	
	private boolean terminated = false;
	
	public void run(){
		while(!terminated){
			if(clientSocket == null || !clientSocket.isConnected() || clientSocket.isClosed() || !clientSocket.isBound())
				terminated = true;
			else{
				try{
					String clientSentence = inFromClient.readLine(); 
					System.out.println(clientSentence);
					if(clientSentence != null)
						parse(clientSentence);
				} catch(Exception e){
					e.printStackTrace();
					System.err.println("Error in TCPServer run");
				}
			}
		}
	}
	
	private boolean lastBreak = false;
	private LinkedList<String> totalCommand;
	private boolean firstCommandCorrect = false;
	private int version;
	private String returnString;
	
	private void parse(String parsing){
		if(parsing.equals("") && !lastBreak){
			lastBreak = true;
		} else if(parsing.equals("") && lastBreak){
			lastBreak = false;
			doSomething();
		} else {
			totalCommand.add(parsing);
		}
	}
	
	private void doSomething(){
		while(!totalCommand.isEmpty()){
			String command = totalCommand.poll();
			if(command.equals("")){
				if(firstCommandCorrect && version == 0){
					sendBack(returnString);
				}
			}
			else{
				String[] commands = command.split("[ ]+");
				if(commands[0].equals("GET"))
					get(commands);
				else if(command.equals("HEAD"))
					head(commands);
				else if(commands[0].equals("PUT"))
					put(commands);
				else if(commands[0].equals("POST"))
					post(commands);
				else
					other(commands);
				}
		}
	}
	
	private void get(String[] commands){
		if(commands.length == 3 || commands.length == 2){
			if(commands.length == 3){
				String versionString = commands[2];
				if(versionString.equals("HTTP/1.0"))
					version = 0;
				else 
					version = 1;
			}
			lastCommand = COMMAND.GET;
			String toPrint = "";
			if(commands[1].equals("/forbiddenPage"))
				toPrint = "Error 403, Forbidden page.";
			else {
				File file = new File("/webpages" + commands[1]);
				try{
					BufferedReader fileReader = new BufferedReader(new FileReader(file));
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
				returnString = toPrint;
				firstCommandCorrect = true;
			}
		}
	}
	
	private void head(String[] commands){
		lastCommand = COMMAND.HEAD;
	}
	
	private void put(String[] commands){
		lastCommand = COMMAND.PUT;
	}
	
	private void post(String[] commands){
		lastCommand = COMMAND.POST;
	}
	
	private void other(String[] commands){
		if(firstCommandCorrect && version == 1 && commands.length > 3){
			if(commands[0].equalsIgnoreCase("Host:") && commands[1].equalsIgnoreCase("localhost:6789"))
				sendBack(returnString);
		}
		else if(firstCommandCorrect)
			sendBack(returnString);
		lastCommand = COMMAND.NONE;
	}
	
	private void sendBack(String toPrint){
		if(!returnString.equals("")){
			try{
				outToClient.writeBytes(returnString);
			} catch(Exception e){
				System.err.println("Error in sendBack @ TCPServer");
			}
		}
		returnString = "";
	}
	
	private COMMAND lastCommand = COMMAND.NONE;
	
	private enum COMMAND{
		GET, HEAD, PUT, POST, NONE;
	}
} 