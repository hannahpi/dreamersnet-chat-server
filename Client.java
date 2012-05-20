package net.dreamersnet.ChatServer;
import java.io.*;
import java.net.*;

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
				//System.out.println(message);
			
				/*BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println(bufferedReader.readLine());*/
		} catch (Exception e) {
			System.out.println("IH(run) exception has occured : " + e);
		}
		
	}
	
	public String getMessage() {
		return message;
	}
}

public class Client {
	static String name;
	static String host="home.dreamersnet.net";
	static Socket socket;
	static int nreq = 0;
	static Client cli = new Client();
	static Thread conInThread;
	static boolean quit = false;
	
	public static void main(String[] args) {
		//For testing:
		if (args.length>0)
			name = args[0];
		if (args.length==2) {
			System.out.print("set name to:" + args[0]);
			System.out.println("   set host to:" + args[1]);
			host = args[1];
		}
		
		if (args.length==0) {
			System.out.print("Set Name: ");
			try {
				BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
				name = consoleIn.readLine();
			} catch (IOException e) {
				System.out.println("Unknown console error : " +e );
			}
		}
		
		if ((args.length>=1) || (name.length()>1)) {
			try {
				socket = new Socket(host, 4444);
				cli.sendRaw(name + " has connected!");
				conInThread = new InputHandler(cli, socket, ++nreq, name);
				conInThread.start();
				while (!quit)
				{
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					System.out.println(bufferedReader.readLine());
				}
				socket.close();
			} catch (Exception e) {
				System.out.println("main exception has occured : " + e);
				System.exit(1);
			}	
		} else {
			System.err.println("Usage: Client <name>");
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
			System.out.println("Client SendMessage Error occured: " + e);
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
			System.out.println("Client SendMessage Error occured: " + e);
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
				System.out.println("ClientCommand Error occured: " + e);
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
				sendRaw("*** Client exiting: " + name + "Reason: " + words[1]);
				socket.close();
			} catch (IOException e) {
				System.out.println("Client Command Error occured: " + e);
				System.exit(1);
			}
		}
		else {
			sendRaw("!#@" + name + " " + words[0] + " " + words[1]);			
		}
	}
}