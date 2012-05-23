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
				socket.close();
			} catch (Exception e) {
				app.println("main exception has occured : " + e);
				System.exit(1);
			}	
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
				sendRaw("*** Client exiting: " + name + " QUIT. ");
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
		if (words[0].compareToIgnoreCase("quit")==0) {
			try {
				sendRaw("*** Client exiting: " + name + " Reason: " + words[1]);
				socket.close();
			} catch (IOException e) {
				app.println("Client Command Error occured: " + e);
				System.exit(1);
			}
		}
		else {
			sendRaw("!#@" + name + " " + words[0] + " " + words[1]);			
		}
	}
}