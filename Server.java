/**
 * ChatServer - Server class
 * @author Waterlgndx
 * 05/20/2012
 * Server is a class that hosts the chat.  I admit it is probably a bit messy.  I also find it strange that I 
 * ended up using an Array of ThreadHandlers instead of a hash of some sort.  I would like to use a hash for
 * commands or command aliases I would like to add ServerObjects which users would be able to interact with
 * and set behavior/properties via the command aliases developed.  But this may be much further down the road. 
 * 
 * classes: Server, ThreadHandler
 */


package net.dreamersnet.ChatServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * ThreadHandler : a class that handles threads for the sockets for connected user.  These threads handle
 *                 incoming and outgoing messages with regards to the users connected.
 * @author Ben Parker
 */
class ThreadHandler extends Thread {
	Socket socket;
	Server serv;
	String userName = new String(" ");
	String message = new String (" ");	
	boolean terminated = false;
	int n;			//Thread Number
	
	ThreadHandler(Server serv, Socket s, int v) {
		this.serv = serv;
		socket = s;
		n= v;
	}
	
	//TODO: Change so that the Server side completely manages the user name after connection...
	public void run()
	{
		try {
			// prime the loop and get user name and get myself added to the server...
			//Read incoming message.  In future versions it will not be important
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
			//PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
			message = bufferedReader.readLine();
			if ((message.indexOf("!#@") >= 0) && (message.indexOf("!#@") < 4)) {					
				String[] tmp = message.split("!#@",2);
				message = tmp[1];
				String[] tmp2 = message.split(" ",2);
				userName = tmp2[0];
				message = tmp2[1];
				serv.sendCommand(userName, message);
			} else {
				String[] tmp = message.split(" ",2);
				userName = tmp[0];
				message = tmp[1];
				if (message.length() > 0)
				{
					System.out.println("<" + userName + "> " + message);
					//Respond to client echoing back the incoming client message		
					serv.sendToAll(this, message);
				}			
			}

			//add me
			serv.addMe(this); 
			
			
			// continue execution as normal...
			while (!terminated) {
				//Read incoming message.  In future versions it will not be important
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
				//PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
				message = bufferedReader.readLine();				
				if ((message.indexOf("!#@") >= 0) && (message.indexOf("!#@") < 4)) {					
					String[] tmp = message.split("!#@",2);
					message = tmp[1];
					String[] tmp2 = message.split(" ",2);
					userName = tmp2[0];
					message = tmp2[1];
					serv.sendCommand(userName, message);
				} else {
					String[] tmp = message.split(" ",2);
					userName = tmp[0];
					message = tmp[1];
					if (message.length() > 0)
					{
						System.out.println("<" + userName + "> " + message);
						//Respond to client echoing back the incoming client message		
						serv.sendToAll(this, message);
					}			
				}
			}			
		} catch (IOException e) {
			System.out.println("ThreadHandler exception occured : " + e);
			System.out.println("Closing socket");
			try {
				socket.close(); 				
				terminated = true;
			} catch (Exception e2) { 
				System.out.println("Close socket failed" + e); 
			}
			return;
		}		
	}

	public Socket getSocket()
	{
		return socket;
	}
	
	public void sendMessage(String from, String msg)
	{
		if (terminated) return;
		try {
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
			if (msg.length() > 0) {
				printWriter.println("<" + from + "> " + msg);
				printWriter.flush();
			}
		} catch (IOException e) {
			System.out.println("Message not sent, exception : " + e);
			System.out.println("Closing socket and attempting recovery");
			try {
				serv.sendRawToAll(userName + " had their connection reset! ");
				socket.close();
				serv.removeThread(this);
				terminated = true;
			} catch (Exception e2) { 
				System.out.println("Close socket failed" + e); 
			}
			return;
		}
	}
	
	//TODO: boolean setSocket() if host names match.
	
	public void setUserName(String newUserName)
	{
		this.userName = newUserName;
		sendRaw("@#!set name " + this.userName);
	}
	
	/**
	 * Deprecated
	 * @return message
	 * this will be removed in next update because of its unreliability.
	 */
	public String getMessage()
	{
		return message;
	}
	
	public String getUserName()
	{
		return userName;
	}
	
	public void sendRaw(String raw)
	{
		if (terminated) return;
		try {
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
			if (raw.length() > 0) {
				printWriter.println(raw);
				printWriter.flush();
			}
		} catch (IOException e) {
			System.out.println("Raw not sent, exception : " + e);
			System.out.println("Closing socket and attempting recovery");
			try {
				socket.close();
				serv.removeThread(this);
				terminated = true;
			} catch (Exception e2) { 
				System.out.println("Close socket failed: " + e); 
			}
			return;
		}
	}
	
	public boolean isClosed()
	{
		return socket.isClosed();
	}
	
	public void terminate()
	{
		terminated = true;		
	}
	
}

public class Server {
	static ArrayList<ThreadHandler> threads = new ArrayList<ThreadHandler>();
	static ArrayList<ThreadHandler> noCXNt = new ArrayList<ThreadHandler>(); //non connected threads
	static Server serv = new Server();
		
	public static void main(String[] args) {
		int nreq = 1;
		try {
			ServerSocket serverSocket = new ServerSocket(4444);
			for (;;) {
				Socket socket = serverSocket.accept();				
				//System.out.println("Creating a new thread ...");
				Thread t = new ThreadHandler(serv, socket, ++nreq);
				t.start();				
				noCXNt.add((ThreadHandler) t); // need to keep it in scope ?
			}
		} catch (IOException e) {
			System.out.println("exception occured : " + e);
			System.exit(-1);
		}
	}
	
	public synchronized void addMe(Thread t) {
		ArrayList<Thread> toRemove = new ArrayList<Thread>();
		ThreadHandler tempHandle = (ThreadHandler) t;
		String attemptName = tempHandle.getUserName();
		for (int i=0; i<threads.size(); i++) {
			if (threads.get(i).isClosed()) {
				toRemove.add(threads.get(i));
				continue;
			}
			if (threads.get(i).getUserName().compareToIgnoreCase(attemptName)==0) {
				System.out.println(attemptName + " is currently in use.");
				//match found terminate session with reason.
				tempHandle.sendMessage("SERVER", "This name is in use!!!  You will be disconnected!!");
				tempHandle.sendMessage("SERVER", "This name is in use!!!  You will be disconnected!!");
				//TODO: change it so client is given a chance to change their name
				//      or if hosts match replace the old connection with the new one.
				//      with setSocket.
				tempHandle.terminate();				
				System.out.println("Username "+ attemptName + " is a duplicated nickname from host: "+ tempHandle.getSocket().getInetAddress().getHostName());
				noCXNt.remove(tempHandle);
				return;
			}											
		}
		threads.add(tempHandle);
		noCXNt.remove(tempHandle);
		tempHandle.sendMessage("SERVER", "Welcome to home.dreamersnet.net!");
		tempHandle.sendMessage("SERVER", "This is an experiemental chat server!");
		for (int i=0; i<toRemove.size();i++)
			removeThread(threads.get(i));
		tempHandle.sendMessage("SERVER", "Just added : /msg and /me commands");
		tempHandle.sendMessage("SERVER", "If you see a name with [PM] that indicates it is a private message");
		
		//String userName = ((ThreadHandler) t).getUserName();
	}
	
	public synchronized void sendToAll(Thread t, String msg)
	{
		String userName = ((ThreadHandler) t).getUserName();
		//send same message to everyone		
		if (msg.length() > 0) {			
			for (int i=0; i<threads.size(); i++) {
				if (threads.get(i).isClosed())
					continue;
				threads.get(i).sendMessage(userName, msg);
			}	
		}
	}
	
	public synchronized void sendToAll(String from, String msg)
	{				
		for (int i=0; i<threads.size(); i++) {
			if (threads.get(i).isClosed())
				continue;
			threads.get(i).sendMessage(from, msg);
		}
		
	}
	
	public synchronized void sendRawToAll(String raw)
	{				
		for (int i=0; i<threads.size(); i++) {
			if (threads.get(i).isClosed())
				continue;
			threads.get(i).sendRaw(raw);
		}
	}
	
	public synchronized void removeThread(Thread t)
	{
		threads.remove(t);
	}
	
	private void sendNamesTo(String from) {
		String toSend = "";
		int nameCt = 0;
		int memory = -1;
		for (int i=0; i<threads.size(); i++) {
			if (threads.get(i).isClosed())
				continue;
			if (threads.get(i).getUserName().compareToIgnoreCase(from)==0)
				memory = i;
			toSend += " " + threads.get(i).getUserName();
			nameCt++;
		}
		if (memory!=-1)
			threads.get(memory).sendRaw("* There are "+ nameCt +" users logged in : " + toSend);		
	}
	
	public void sendCommand(String from, String command) 
	{		
		// NAMES, WHO command are currently the same thing
		if ((command.compareToIgnoreCase("names")== 0) || (command.compareToIgnoreCase("who")==0)) {
			sendNamesTo(from);
			return;
		}
		String[] words = command.split(" ", 3);
		// NICK, NICKNAME command
		if ((words[0].compareToIgnoreCase("nickname")==0)||(words[0].compareToIgnoreCase("nick")==0)) {
			boolean newNickOk = true;
			int userThread = -1;
			for (int i=0; i<threads.size(); i++)
			{
				if (threads.get(i).isClosed())
					continue;
				if (threads.get(i).getUserName().compareToIgnoreCase(words[1])==0) {
					newNickOk = false;
				}
				else if (threads.get(i).getUserName().compareToIgnoreCase(from)==0)
					userThread = i;
			}
			if ((newNickOk) && (userThread >= 0)) {
				//change nick and let everyone know
				threads.get(userThread).setUserName(words[1]);
				sendRawToAll("*** "+ from + " is now known as " + words[1]);
			} else {
				threads.get(userThread).sendRaw("Nickname changed failed!  It appears it is already in use!");
			}
			return;
		}
		// MSG , TELL commands  (many of my friends are ex-mmo players, adding these for ease)
		else if ((words[0].compareToIgnoreCase("msg")== 0) || (words[0].compareToIgnoreCase("tell")== 0)) {			
			for (int i=0; i<threads.size(); i++) {
				if (threads.get(i).isClosed())
					continue;
				if (threads.get(i).getUserName().compareToIgnoreCase(words[1])==0) {
					threads.get(i).sendMessage("[PM] "+ from, words[2]);
					return;
				}				
			}			
		}
		// ME , EMOTE commands
		else if ((words[0].compareToIgnoreCase("me")== 0) || (words[0].compareToIgnoreCase("emote")== 0)) {
			String tmp="";
			for (int i=1; i<words.length; i++)
				tmp += words[i] + " ";
			sendRawToAll("ACTION: * " + from + " "+ tmp.trim());
		}
		else {
			try {
				System.out.println("Unknown command raw: " + command);
				System.out.println("words[0]:" + words[0]);
				System.out.println("words[1]:" + words[1]);
				System.out.println("words[2]:" + words[2]);
			} catch (Exception e) {
				System.out.println("server sendCommand exception: " + e);				
			}			
		}
	}
}
