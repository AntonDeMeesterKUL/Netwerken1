package networks1;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageGetter implements Runnable{

	public void run(){
		outToServer.println("GET " + url.getFile() + " " + port + " HTTP/1.1");
		outToServer.println("Host: " + url.getHost() + ":" + port);
		outToServer.println();
		System.out.println("GET " + url.getFile() + " " + port + " HTTP/1.1");
		System.out.println("Host: " + url.getHost() + ":" + port);
		try{
			BufferedImage image = ImageIO.read(inFromServer);
			File outputFile = new File(filePath + file);
			//if(image != null)
				ImageIO.write(image, "png", outputFile);
			socket.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private Socket socket;
	private String file;
	private URL url;
	private int port;
	private PrintWriter outToServer;
	private BufferedInputStream inFromServer;
	private String filePath = "/src/networks1/images/";
	
	public ImageGetter(Socket socket, URL url, int port, String imageFile){
		//this.socket = socket;
		file = imageFile;
		this.port = port;
		try{
			outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			inFromServer = new BufferedInputStream(socket.getInputStream());
			this.url = new URL("http:" + imageFile);
			this.socket = new Socket(url.getHost(), port);
		} catch(Exception e){
			System.err.println("Error in constructor of ImageGetter");
		}
	}
		
}
