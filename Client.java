package net.dreamersnet.ChatServer;
import java.io.*;
import java.net.*;
/**
 * removing
 * @author Gumba
 *
 *
class InputHandler extends Thread
{
	int n;
	Socket socket;
	Client cli;
	String message = "";
	String userName;
	boolean quit;
	
	InputHandler(Client cli, Socket s, int v, String name)
	{
		this.cli = cli;
		socket=s;
		n = v;
		userName = name;
	}
	
	//TODO: for duplicated nicknames there needs to be a way to negotiate
	//      with server and establish a new nickname.  Need to be able to detect
	//      signal when it needs changed though.  
	
	public void run() {
		try {
			while (!quit) {
				BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
				String message = consoleIn.readLine();
				if ((message.length()>0) && (message.indexOf("/") == 0)) {
					message = message.substring(1);	
					cli.sendCommand(message);
				} else {
					cli.sendMessage(message);
				}
			}
				//output.println(message);
			
				/*BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output.println(bufferedReader.readLine()); **
		} catch (Exception e) {
			System.out.println("IH(run) exception has occured : " + e);
		}
		
	}
	
	public String getMessage() {
		return message;
	}
}
*/


/**
 * Client - a class to connect to the simple chat server I'm designing.
 * @author Waterlgndx
 *
 */
public class Client {
	//You may want to change this!!!
	final static String DEFAULT_HOST = "home.dreamersnet.net"; 
	final static int DEFAULT_PORT = 4444;
	//TODO: add a way to configure this ( Config file? ) 
	static String name;
	static String host= DEFAULT_HOST;
	static int port= DEFAULT_PORT;
	static Socket socket;
	static int nreq = 0;
	static Client cli = new Client();
	static AppWindow app = new AppWindow(cli);
	static Thread conInThread;
	static boolean quit = false;
	//static PrintStream output = new PrintStream(app.getOutputStream());
	
	Client () {
	}
	
		
	public static void main(String[] args) {
		if (args.length>0)
			name = args[0];
		if (args.length==2) {
			host = args[1];
		}
		
		if (args.length==0) {
			app.print("Set Name: ");
			try {
				BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
				name = consoleIn.readLine();
			} catch (IOException e) {
				app.println("Unknown console error : " +e );
			}
		}
		AppWindow.run();
		if ((args.length>=1) || (name.length()>1)) {
			try {
				socket = new Socket(host, DEFAULT_PORT);
				cli.sendRaw(name + " has connected!");				
				//conInThread = new InputHandler(cli, socket, ++nreq, name);
				//conInThread.start();
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
		} else {
			app.println("Usage: Client <name>");
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