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
import java.util.Date;

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
			// prime the loop and get user name and get client added to the server...
			//Read incoming message.  In future versions it will not be important
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
			message = bufferedReader.readLine();
			if (message.isEmpty()) {
				this.terminate();
				return;
			}
			if ((message.indexOf("!#@") >= 0) && (message.indexOf("!#@") < 4)) {	//this might now be deprecated					
				String[] tmp = message.split("!#@",2);
				message = tmp[1];
				String[] tmp2 = message.split(" ",2);
				userName = tmp2[0];
				message = tmp2[1];
				serv.sendCommand(userName, message);
			} else if ((message.indexOf("+++") >= 0) && (message.indexOf("+++") <4)) {
				String[] tmp = message.split(" ",2);
				userName = tmp[1];
			}
			else {
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
				message = bufferedReader.readLine();				
				if ((message.indexOf("!#@") >= 0) && (message.indexOf("!#@") < 4)) {  //this might now be deprecated					
					String[] tmp = message.split("!#@",2);
					message = tmp[1];
					String[] tmp2 = message.split(" ",2);
					userName = tmp2[0];
					message = tmp2[1];
					serv.sendCommand(userName, message);
				}  else {
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
			Date d = new Date();			
			System.out.println(d + ">>  ThreadHandler exception occured : " + e);  
			e.printStackTrace();
			System.out.println("Closing socket");
			try {
				terminate();
				socket.close(); 				
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
	
	public void setSocket(Socket s) {
		socket = s;
	}
	
	public synchronized void sendMessage(String from, String msg)
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
		try {	
			socket.close();
			serv.removeThread(this);
		} catch ( Exception e) {
			System.out.println("Error terminating socket for " + userName + ": "+ e);
		}
		
		try {
			serv.sendRawToAll(userName + " lost connection! ");
		} catch (Exception e) {
			System.out.println("Error sending message for "+ userName +": " + e);
		}
	}
	
	public void terminateT()
	{
		terminated = true;
		serv.removeThread(this);
		return;
	}
	
}

public class Server {
	static ArrayList<ThreadHandler> threads = new ArrayList<ThreadHandler>();
	static ArrayList<ThreadHandler> noCXNt = new ArrayList<ThreadHandler>(); //non connected threads
	static Server serv = new Server();
		
	public static void main(String[] args) {
		int nreq = 1;
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(5999);
			for (;;) {
				Socket socket = serverSocket.accept();				
				Thread t = new ThreadHandler(serv, socket, ++nreq);
				t.start();				
				noCXNt.add((ThreadHandler) t); 
			}
		} catch (IOException e) {
			System.out.println("exception occured : " + e);
			System.exit(-1);
		}
	}
	
	public synchronized void addMe(Thread t) {
		ArrayList<Thread> toRemove = new ArrayList<Thread>();
		ThreadHandler newThread = (ThreadHandler) t;   //new thread
		String attemptName = newThread.getUserName();
		for (int i=0; i<threads.size(); i++) {
			if (threads.get(i).isClosed()) {
				toRemove.add(threads.get(i));
				continue;
			}
			if (threads.get(i).getUserName().compareToIgnoreCase(attemptName)==0) {
				System.out.println(attemptName + " is currently in use.");
				//match found terminate session with reason.
				ThreadHandler oldThread = threads.get(i);   //old thread
				if (newThread.getSocket().getInetAddress().toString().compareToIgnoreCase(oldThread.getSocket().getInetAddress().toString())==0) {  
					// if addresses match replace old session
					newThread.sendMessage("SERVER", "This name is in use, but your host matches, replacing session.");
					oldThread.sendMessage("SERVER", "We believe you have been a ghosted user, killing your session.");
					oldThread.setSocket(newThread.getSocket());
					serv.removeThread(newThread);  //set the socket so we don't need newThread any more.
					System.out.println("Username "+ attemptName + " is a duplicated nickname from host: "+ newThread.getSocket().getInetAddress().getHostName());
					System.out.println("Server detected the host to be a match from it's previous connection so has replaced the connection with the current one");
					noCXNt.remove(newThread);
					return;
				} else {
					newThread.sendMessage("SERVER", "This name is in use!!!  You will be disconnected!!");
					newThread.sendMessage("SERVER", "This name is in use!!!  You will be disconnected!!");
					//TODO: change it so client is given a chance to change their name
					newThread.terminate();				
					System.out.println("Username "+ attemptName + " is a duplicated nickname from host: "+ newThread.getSocket().getInetAddress().getHostName());
					System.out.println("Original host is " + oldThread.getSocket().getInetAddress().getHostName());
					noCXNt.remove(newThread);
					return;
				}
			}											
		}
		threads.add(newThread);
		noCXNt.remove(newThread);
		newThread.sendMessage("SERVER", "Welcome to home.dreamersnet.net!");
		newThread.sendMessage("SERVER", "This is an experiemental chat server!");
		for (int i=0; i<toRemove.size();i++)
			removeThread(toRemove.get(i));
		newThread.sendMessage("SERVER", "Just added : /msg and /me commands");
		newThread.sendMessage("SERVER", "If you see a name with [PM] that indicates it is a private message");
		sendRawToAll("*** JOINED: " + newThread.getUserName() + " is now connected and is chatting!");
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
	
	private synchronized void sendNamesTo(String from) {
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
	
	public synchronized void sendCommand(String from, String command) 
	{		
		// Make sure to return after performing the actions required for your command!
		
		// NAMES, WHO command are currently the same thing
		if ((command.compareToIgnoreCase("names")== 0) || (command.compareToIgnoreCase("who")==0)) {
			sendNamesTo(from);
			return;
		}
		// QUIT command
		if (command.compareToIgnoreCase("quit")==0) {
			int userThread = -1;
			for (int i=0; i<threads.size(); i++)
			{
				if (threads.get(i).getUserName().compareToIgnoreCase(from)==0)
					userThread = i;
			}
			if ( userThread >= 0 ) {
				sendRawToAll("*** Client exiting : " + from);
				threads.get(userThread).terminate();
				threads.remove(userThread);
			}
			return;				
		}
		//Multiple parameter commands:
		String[] words = command.split(" ", 3);
		
		// QUIT with parameters
		if (words[0].compareToIgnoreCase("quit")==0) {
			int userThread = -1;
			for (int i=0; i<threads.size(); i++)
			{
				if (threads.get(i).getUserName().compareToIgnoreCase(from)==0)
					userThread = i;
			}
			
			if ( userThread >= 0 ) {
				if (words.length==2) 
					sendRawToAll("*** Client exiting : " + from + " with reason : " + words[1]);
				else
					sendRawToAll("*** Client exiting : " + from + " with reason : " + words[1] + " " + words[2]);
				threads.get(userThread).terminate();
				threads.remove(userThread);
			}
			return;				
		}
		// NICK, NICKNAME command
		if ((words[0].compareToIgnoreCase("nickname")==0)||(words[0].compareToIgnoreCase("nick")==0)) {
			boolean newNickOk = true;
			int userThread = -1;
			for (int i=0; i<threads.size(); i++)
			{
				if (threads.get(i).isClosed())
					continue;
				String other = threads.get(i).getUserName();
				if (other.compareToIgnoreCase(from)==0) {   //allow users to change nickname to variations with case
					userThread = i;
					continue;
				}
				if (other.compareToIgnoreCase(words[1])==0) {
					newNickOk = false;
				}				
			}
			if ((newNickOk) && (userThread >= 0)) {
				//change nick and let everyone know
				threads.get(userThread).setUserName(words[1]);
				sendRawToAll("*** "+ from + " is now known as " + words[1]);
			} else if (userThread>0) {
				threads.get(userThread).sendRaw("Nickname changed failed!  It appears it is already in use!");
			} else {
				System.out.println("Error no user found: "+ from);
			}
			return;
		}
		// MSG , TELL commands  (many of my friends are ex-mmo players, adding these for ease)
		else if ((words[0].compareToIgnoreCase("msg")== 0) || (words[0].compareToIgnoreCase("tell")== 0) || (words[0].compareToIgnoreCase("t")==0)) {		
			boolean sent = false;
			int fromUser = -1;
			for (int i=0; i<threads.size(); i++) {
				if (threads.get(i).isClosed())
					continue;
				if (threads.get(i).getUserName().compareToIgnoreCase(words[1])==0) {
					threads.get(i).sendMessage("[PM] "+ from, words[2]);
					sent = true;
				}
				if (threads.get(i).getUserName().compareToIgnoreCase(from)==0)
					fromUser = i;								
			} //end for loop
			if ((fromUser >= 0) && sent) {
				threads.get(fromUser).sendMessage("Sent PM["+ words[1] + "]", ": " + words[2]);
			}
			return;
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
