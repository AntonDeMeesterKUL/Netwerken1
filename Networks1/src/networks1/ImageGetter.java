package networks1;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class ImageGetter implements Runnable{

	// GET nl.wikipedia.org/wiki/Hoofdpagina 80 HTTP/1.1
	public void run(){
//		outToServer.println("GET " + url.getFile() + " " + port + " HTTP/1.1");
//		outToServer.println("Host: " + url.getHost() + ":" + port);
//		outToServer.println();
//		System.out.println("GET " + url.getFile() + " " + port + " HTTP/1.1");
//		System.out.println("Host: " + url.getHost() + ":" + port);
//		try{
//			BufferedImage image = ImageIO.read(inFromServer);
//			File outputFile = new File(filePath + file);
//			//if(image != null)
//				ImageIO.write(image, "png", outputFile);
//			socket.close();
//		} catch(Exception e){
//			e.printStackTrace();
//		}
		try{

			if(file.toLowerCase().startsWith("http://") || file.toLowerCase().startsWith("www.") || file.indexOf(".") < file.indexOf("/") ){ //full address
				outToServer.println("GET " + file + " " + port + " HTTP/1.1");
				System.out.println("GET " + file + " " + port + " HTTP/1.1");
			}
			else{ //relative address
				outToServer.println("GET " + url.getHost() + file + " " + port + " HTTP/1.1");
				System.out.println("GET " + file + " " + port + " HTTP/1.1"); }
			outToServer.println("Host: " + "nl.wikipedia.org" + ":" + port);
			outToServer.println();
			byte[] buffer = new byte[4096];
		    int bytes_read;
		    OutputStream toFile = new FileOutputStream("src/networks1/receive/image" + Client.fileNumber + ".png");
	    	Client.fileNumber++;
	    	String thing = "";
		    while((bytes_read = inFromServer.read(buffer)) != -1){
		    	System.out.write(buffer, 0, bytes_read);
		    	toFile.write(buffer, 0, bytes_read);
		    	thing += new String(buffer,"UTF-8");
		    }
		    toFile.close();
		} catch(FileNotFoundException fnfe){
			System.out.println("FileNotFoundException in running ImageGetter");
		} catch(IOException ioe){
			System.out.println("IOException in running ImageGetter");
		}
	}
	
	private Socket socket;
	private String file;
	private URL url;
	private int port;
	private PrintWriter outToServer;
	private InputStream inFromServer;
	
	public ImageGetter(Socket socket, URL url, int port, String imageFile) throws IOException{
		//this.socket = socket;
		file = imageFile.substring(2);
		this.port = port;
		outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		inFromServer = socket.getInputStream();
		System.out.println("Created ImageGetter");
		System.out.println(file);
		System.out.println(url);
	}
		
}
