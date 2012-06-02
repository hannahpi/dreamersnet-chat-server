package net.dreamersnet.ChatServer;
import java.io.*;
import java.net.*;

/**
 * Client - a class to connect to the simple chat server I'm designing.
 * @author Waterlgndx
 *
 */
public class Client {
	//You may want to change this!!!
	final static String DEFAULT_HOST = "home.dreamersnet.net"; 
	final static String DEFAULT_NAME = "Anonymous";
	static int attempts = 0;
	final static int DEFAULT_PORT = 4444;
	//TODO: add a way to configure this ( Config file? ) 
	static String name;
	static String host;
	static int port;
	static Socket socket;
	static int nreq = 0;
	static Client cli = new Client();
	static AppWindow app = new AppWindow(cli);
	static Thread conInThread;
	static boolean quit = false;
	
	Client () {
	}
	
		
	public static void main(String[] args) {
		if (args.length>0)
			name = args[0];
		if (args.length==2) {
			host = args[1];
		}
		
		if (args.length==0) {
			name = app.askString("Set Name: ", "Name");
			host = app.askString("Connect to host? ( Press <enter> for home.dreamersnet.net ) ", "Connect");
		}
		
		if (name.length()==0) {
			name= DEFAULT_NAME;
		}
		
		if (host.length()==0) {
			host= DEFAULT_HOST;
		}
		
		AppWindow.run();
		if ((args.length>=1) || (name.length()>1)) {
			try {
				socket = new Socket(host, DEFAULT_PORT);
				socket.setKeepAlive(true);
				cli.sendRaw(name + " has connected!");				
				while (!quit)
				{
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String newLine = bufferedReader.readLine().trim();
					if (newLine.startsWith("@#!set name")) {						
						String[] words = newLine.split(" ",3); // .split ==> ,3) means 3 positions in array are created.
						name = words[2];
						continue;
					} else { 
						app.println(newLine);
					}					
				}				
			} catch (SocketException sexc) {
				repair();
				System.out.println("main exception has occured : " + sexc);
				app.println("main exception has occured : " + sexc);				
				System.exit(1);
			} catch (Exception e) {
				System.out.println("main exception has occured : " + e);
			}
		} 
	}
	
	private static void repair() {
		if (attempts <= 10) {
			System.out.println("Socket \n Closed: " + socket.isClosed() + "\n InputFailure: " + socket.isInputShutdown() + "\n Connected:" + socket.isConnected());
			System.out.println("System exiting...");		
			String[] send = (name + " "+ host).split(" ");
			attempts++;
			main(send);			
		} else {
			app.println("Number of attempts exceeds the limit!");
			System.exit(1);
		}
			
	}
	
	public void sendMessage(String msg)
	{
		//send the message
		try {
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(name + " " + msg); // send it!
			printWriter.flush();
		} catch (IOException e) {
			app.println("Client SendMessage Error occured: " + e);
			System.exit(1);
		}
	}
	
	public void sendRaw(String raw)
	{
		try {
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(raw); // send it!
			printWriter.flush();
		} catch (IOException e) {
			app.println("Client SendMessage Error occured: " + e);
			System.exit(1);
		}
	}
	
	public void sendCommand(String command) {
		if (command.compareToIgnoreCase("quit")==0) {
			try {
				sendRaw("!#@" + name + " " + command);
				quit = true;
				socket.close();
			} catch (IOException e) {
				app.println("ClientCommand Error occured: " + e);
				System.exit(1);
			}		
		} 
		String[] words=command.split(" ", 2);
		if (words.length <= 1) {
			sendRaw("!#@" + name + " " + command);
			return;
		}
		else {
			sendRaw("!#@" + name + " " + words[0] + " " + words[1]);			
		}
	}
}