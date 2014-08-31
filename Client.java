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
	final static int DEFAULT_PORT = 5999;
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
			String tmp = app.askString("Set Name:", "Name");
			name = tmp.split(" ")[0];
			tmp = app.askString("Connect to host? ( Press <enter> for home.dreamersnet.net ) ", "Connect");
			host = tmp.split(" ")[0];
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
				final StringBuilder newLine=new StringBuilder();
				final StringBuilder tmp=new StringBuilder();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (!quit)
				{					
						try {
							newLine.delete(0, newLine.length());
							tmp.delete(0,tmp.length());
							tmp.append(bufferedReader.readLine().trim());
							if (!tmp.toString().isEmpty())									
								newLine.append(tmp);
						} catch(NullPointerException e) {
							try {
								repair();
								e.printStackTrace();
							} catch (Exception e2) { 	}
						} catch (SocketException sexc) {
							repair();
							System.out.println("main exception has occured : " + sexc);
							app.println("main exception has occured : " + sexc);
							sexc.printStackTrace();
							System.exit(1);
						} catch (Exception e) {
							System.out.println("Event Main exception occured");
						}
						
						if (newLine.toString().startsWith("@#!set name")) {						
							String[] words = newLine.toString().split(" ",3); // .split ==> ,3) means 3 positions in array are created.
							name = words[2];
						} else { 
							app.println(newLine.toString());
						}
					
				}
			} catch (SocketException sexc) {  // you're just gonna
				System.out.println("Socket error, no connection could be established."); // just gonna
				sexc.printStackTrace(); // yeah you walk away!
				System.exit(1);  //and just walk away
			} catch (Exception e) {
				System.out.println("main exception has occured : " + e);
				e.printStackTrace();
			}
				
		} 
	}
	
	private static void repair() {
		if (attempts <= 10) {
			System.out.println("Socket \n Closed: " + socket.isClosed() + "\n InputFailure: " + socket.isInputShutdown() + "\n Connected:" + socket.isConnected());
			System.out.println("System exiting...");		
			attempts++;
			try {
				socket = new Socket(host, DEFAULT_PORT);
				socket.setKeepAlive(true);
				cli.sendRaw(name + " has reconnected!");
			} catch ( Exception e) {
				System.out.println("Repair failed");
			}
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
			e.printStackTrace();
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
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void sendCommand(String command) {
		
		String[] words=command.split(" ", 2);
		if (words[0].trim().compareToIgnoreCase("quit")==0) {
			try {
				sendRaw("!#@" + name + " " + command);
				quit = true;
				socket.close();
				return;
			} catch (IOException e) {
				app.println("ClientCommand Error occured: " + e);
				e.printStackTrace();
				System.exit(1);
			}		
		} 
		
		if (words.length <= 1) {
			sendRaw("!#@" + name + " " + command);
			return;
		}
		else {
			sendRaw("!#@" + name + " " + words[0] + " " + words[1]);			
		}
	}
}