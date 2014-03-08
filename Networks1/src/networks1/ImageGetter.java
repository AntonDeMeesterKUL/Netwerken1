package networks1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class ImageGetter implements Runnable{

	public void run(){
		outToServer.println("GET " + url.getFile() + file + " " + port + " HTTP/1.1");
		System.out.println(url.getFile() + file);
		outToServer.println("Host: " + url.getHost() + ":" + port);
		outToServer.println();;	
		try{
			inFromServer.readLine();
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private Socket socket;
	private String file;
	private URL url;
	private int port;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private String filepath = "/src/networks1/images/";
	
	public ImageGetter(Socket socket, URL url, int port, String imageFile){
		this.socket = socket;
		file = imageFile;
		this.url = url;
		this.port = port;
		try{
			outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(Exception e){
			System.err.println("Error in constructor of ImageGetter");
		}
	}
		
}
