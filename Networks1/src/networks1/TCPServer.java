package networks1;
import java.io.*; 
import java.net.*; 
class TCPServer implements Runnable { 
	
	public static void main(String argv[]) throws Exception { 
		ServerSocket welcomeSocket = new ServerSocket(6789); 
		while(true) { 
			Socket clientSocket = welcomeSocket.accept(); 
			if(clientSocket != null){
				TCPServer server = new TCPServer(clientSocket);
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
	}
	
	private boolean terminated;
	
	public void run(){
		while(!terminated){
			try{
				String clientSentence = inFromClient.readLine(); 
				parse(clientSentence);
			} catch(Exception e){
				System.err.println("Error in TCPServer run");
			}
		}
	}
	
	private boolean headLine;
	
	private void parse(String parsing){
		String[] commands = parsing.split("[ ]+");
		if(commands[0].equals("GET"))
			get(commands);
		else if(commands[0].equals("HEAD"))
			head(commands);
		else if(commands[0].equals("PUT"))
			put(commands);
		else if(commands[0].equals("POST"))
			post(commands);
		else
			other(commands);
	}
	
	private void get(String[] commands){
		lastCommand = COMMAND.GET;
		
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
		switch(lastCommand) {
			case GET:
			case HEAD:
			case PUT:
			case POST:
			case NONE:
			default:
				System.err.println("Illegal state of lastCommand in TCPServer");
		}
		lastCommand = COMMAND.NONE;
	}
	
	private COMMAND lastCommand = COMMAND.NONE;
	
	private enum COMMAND{
		GET, HEAD, PUT, POST, NONE;
	}
} 