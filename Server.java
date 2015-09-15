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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



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
				serv.sendRawToAll("*** JOINED: " + userName + " is now connected and chatting!");
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
				message = bufferedReader.readLine();				
				if ((message.indexOf("!#@") >= 0) && (message.indexOf("!#@") < 4)) {  //this might now be deprecated					
					String[] tmp = message.split("!#@",2);
					message = tmp[1];
					String[] tmp2 = message.split(" ",2);
					userName = tmp2[0];
					message = tmp2[1];
					serv.sendCommand(userName, message);
				} else if ((message.indexOf("[#]") >= 0) && (message.indexOf("[#]") < 4)) {
					String[] tmp = message.split(" ", 2);
					userName = tmp[1];
					serv.sendRawNames(userName);
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
	
	public void closeSocket()
	{
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public boolean equals(ThreadHandler th) {
		return (socket.getInetAddress().toString().compareToIgnoreCase(th.getSocket().getInetAddress().toString())==0);		
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
			serv.sendRawNamesAll();
		} catch ( Exception e) {
			System.out.println("Error terminating socket for " + userName + ": "+ e);
		}
		
		try {
			serv.sendRawToAll(userName + " lost connection! ");
			serv.sendRawNamesAll();
		} catch (Exception e) {
			System.out.println("Error sending message for "+ userName +": " + e);
		}
	}
	
	public void terminateT()
	{
		terminated = true;
		serv.removeThread(this);
		serv.sendRawNamesAll();
		return;
	}
	
}

public class Server {
	static HashMap<String, ServerObject> sobj = new HashMap<String, ServerObject>();
	static HashMap<String, ThreadHandler> threads = new HashMap<String, ThreadHandler>();
	static ArrayList<ThreadHandler> noCXNt = new ArrayList<ThreadHandler>(); //non connected threads
	static Server serv = new Server();
		
	public static void main(String[] args) {
		int nreq = 1;
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(5999);
			System.out.println("Server is now listening from " + serverSocket.getInetAddress().toString() + " on " + serverSocket.getLocalPort());
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
		
		threads.forEach((userName, th) -> {
			if (th.isClosed()) 
				toRemove.add(th);	
			
		});
		
		if (threads.containsKey(attemptName))
		{
			System.out.println(attemptName + " is currently in use.");
			//match found terminate session with reason.
			ThreadHandler oldThread = threads.get(attemptName.toLowerCase());   //old thread
			if (newThread.equals(oldThread)) {  
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
		threads.put(newThread.getUserName().toLowerCase(), newThread);
		noCXNt.remove(newThread);
		newThread.sendMessage("SERVER", "Welcome to home.dreamersnet.net!");
		newThread.sendMessage("SERVER", "This is an experiemental chat server!");
		for (int i=0; i<toRemove.size();i++)
			removeThread(toRemove.get(i));
		newThread.sendMessage("SERVER", "Just added : /create <obj> <desc> ; /look [item] ; /destroy <ownObject> ");
		newThread.sendMessage("SERVER", "If you see a name with [PM] that indicates it is a private message");
		newThread.sendMessage("SERVER", "Supported Commands: /me /emote /msg /tell /t /create /destroy /look /help /nickname /nick /?");
		sendRawToAll("*** JOINED: " + newThread.getUserName() + " is now connected and is chatting!");
	}
	
	public synchronized void sendToAll(Thread t, String msg)
	{
		String from = ((ThreadHandler) t).getUserName();
		//send same message to everyone		
		if (msg.length() > 0) {  //can't send an empty message!
			threads.forEach((userName, th) -> {
				if (!th.isClosed())
					th.sendMessage(from,msg);
			});		
		} //TODO:error?
	}
	
	public synchronized void sendToAll(String from, String msg)
	{	
		threads.forEach((userName, th) -> {
			if (!th.isClosed())
				th.sendMessage(from,msg);
		});		
	}
	
	public synchronized void sendRawToAll(String raw)
	{		
		threads.forEach((userName, th) -> {
			if (!th.isClosed())
				th.sendRaw(raw);
		});				
	}
	
	public synchronized void removeThread(Thread t)
	{
		threads.remove(t);
	}
	
	private synchronized void sendNamesTo(String from) {
		String toSend = new String();
		int nameCt = 0;
		from = from.toLowerCase();
		if (threads.containsKey(from))
		{
			ThreadHandler thFrom = threads.get(from.toLowerCase());
		
			if (thFrom.isClosed())
				return;	
			
			//create list of users
			String[] users = new String[threads.size()];
			users = threads.keySet().toArray(users);
			for (String user:users){
				if (threads.get(user.toLowerCase()).isClosed())
					continue;
				toSend += " " + threads.get(user.toLowerCase()).getUserName();
				nameCt++;
			}
			thFrom.sendRaw("* There are "+ nameCt + " users logged in : " + toSend);		
		}						
	}
	
	public synchronized void sendRawNames(String from) {
		String toSend = new String();		
		from = from.toLowerCase();
		if (threads.containsKey(from))
		{
			ThreadHandler thFrom = threads.get(from.toLowerCase());
		
			if (thFrom.isClosed())
				return;	
			
			//create list of users
			String[] users = new String[threads.size()];
			users = threads.keySet().toArray(users);
			for (String user:users){
				if (threads.get(user.toLowerCase()).isClosed())
					continue;
				toSend += " " + threads.get(user.toLowerCase()).getUserName();
			}
			thFrom.sendRaw("[#] " + toSend);		
		}
	}
	
	public synchronized void sendRawNamesAll() {
		String toSend = new String();		
		
		//create list of users
		String[] users = new String[threads.size()];
		users = threads.keySet().toArray(users);
		for (String user:users){
			if (threads.get(user.toLowerCase()).isClosed())
				continue;
			toSend += " " + threads.get(user.toLowerCase()).getUserName();
		}
		serv.sendRawToAll("[#] " + toSend);		
	}
	
	public synchronized void createObject(String itemName, String from, String desc )
	{
		ServerObject so = new ServerObject(this, itemName, from, desc) ;
		sendRawToAll(from + " created " + itemName + " (" + desc + ") ");
		sobj.put(itemName, so);
	}
	
	public synchronized void sendCommand(String from, String command) 
	{		
		// Make sure to return after performing the actions required for your command!
		
		// NAMES, WHO command are currently the same thing
		if ((command.compareToIgnoreCase("names")== 0) || (command.compareToIgnoreCase("who")==0)) {
			sendNamesTo(from);
			return;
		}
		
		// ?, HELP commands
		if ((command.compareToIgnoreCase("help")==0) || (command.compareTo("?")==0)) {
			ThreadHandler userThread = threads.get(from.toLowerCase());
			
			if (userThread != null)
			{				
				userThread.sendRaw("The following commands are available:");
				userThread.sendRaw("ACTION: /me <text> (alias: /emote)");
				userThread.sendRaw("PRIVATE MESSAGE: /msg <nickname> <msg> (alias: /t, /tell)");
				userThread.sendRaw("HELP: /help (alias: /?)");
				userThread.sendRaw("CREATE OBJECT: /create <object> <description>");
				userThread.sendRaw("DESTROY OBJECT: /destroy <ownObject> ");
				userThread.sendRaw("LOOK (all or item): /look [item]");
				userThread.sendRaw("CHANGE NICKNAME: /nickname <newName> (alias /nick);");
				userThread.sendRaw("Note: <parameter> , [optional parameter] ");
			}
			return;
		}
		
		// QUIT command
		if (command.compareToIgnoreCase("quit")==0) {
			ThreadHandler userThread = threads.get(from.toLowerCase());
			
			if ( userThread != null ) {
				sendRawToAll("*** Client exiting : " + from);
				userThread.terminate();
				userThread.closeSocket();
				threads.remove(from);
			}
			return;				
		}
		
		if (command.compareToIgnoreCase("look")==0) {
			final ThreadHandler userThread = threads.get(from.toLowerCase());
						
			if (sobj.isEmpty())
				userThread.sendRaw("No items yet!");
			if (userThread != null ) {
				sobj
					.forEach((k, so) -> userThread.sendRaw(so.getName() + 
								"..." + so.getDesc() + "..." + so.getStat()));
			}
			return;
		}
		
		//Multiple parameter commands:
		String[] words = command.split(" ", 3);
		
		// CREATE command
		if (words[0].compareToIgnoreCase("create")==0) {
			createObject(words[1], from, words[2]);
			return;
		}
		
		// Get desc 
		if (words[0].compareToIgnoreCase("look")==0)
		{
			ServerObject so = sobj.get(words[1]);
			
			ThreadHandler userThread = threads.get(from.toLowerCase());
			if (so == null && userThread != null) {
				userThread.sendRaw("No such object!");				
			} else if (userThread != null) {
				userThread.sendRaw("You inspect " + so.getName() + " it is " + so.getDesc() );
			}
			return;
		}
		if (words[0].compareToIgnoreCase("destroy")==0) {
			ServerObject so = sobj.get(words[1]);
			ThreadHandler userThread = threads.get(from.toLowerCase());
			
			if (so.getCreator().compareToIgnoreCase(from)==0) {
				sobj.remove(words[1]);
				this.sendRawToAll("Object " + words[1] + " removed.");
			} else {
				userThread.sendRaw(so.getName() + " doesn't listen to you!");
			}
			return;
		}
		
		if (words[0].compareToIgnoreCase("stats")==0)
		{
			ServerObject so = sobj.get(words[1]);
			ThreadHandler userThread = threads.get(from.toLowerCase());
			if (userThread != null) {
				userThread.sendRaw("You inspect " + so.getName() + " it is " + so.getDesc() );
				userThread.sendRaw("Further inspection reveals its stats...");
				userThread.sendRaw(so.getStat());
			}
			return;
		}
		
		// QUIT with parameters
		if (words[0].compareToIgnoreCase("quit")==0) {
			ThreadHandler userThread = threads.get(from.toLowerCase());
			
			
			if ( userThread != null ) {
				if (words.length==2) 
					sendRawToAll("*** Client exiting : " + from + " with reason : " + words[1]);
				else
					sendRawToAll("*** Client exiting : " + from + " with reason : " + words[1] + " " + words[2]);
				userThread.terminate();
				threads.remove(from);
			}
			return;				
		}
		
		// NICK, NICKNAME command
		if ((words[0].compareToIgnoreCase("nickname")==0)||(words[0].compareToIgnoreCase("nick")==0)) {
			boolean newNickOk = true;
			ThreadHandler userThread = threads.get(from.toLowerCase());
			String[] users = new String[threads.size()];
			users = threads.keySet().toArray(users);
			String attemptName = words[1];
			if (attemptName.compareToIgnoreCase(from)==0) {
				userThread.setUserName(attemptName);
				sendRawToAll("*** "+ from + " is now known as " + attemptName);
				return;
			}
			if (threads.containsKey(attemptName.toLowerCase()) && (attemptName.compareToIgnoreCase(from)!=0))
				newNickOk=false;
			if ((newNickOk) && (userThread != null)) {
				//change nick and let everyone know
				userThread.setUserName(attemptName);
				threads.put(userThread.getUserName().toLowerCase(), userThread);
				threads.remove(from.toLowerCase());				
				sendRawToAll("*** "+ from + " is now known as " + words[1]);
			} else if (!newNickOk) {
				userThread.sendRaw("Nickname changed failed!  It appears it is already in use!");
			} else {
				System.out.println("Error no user found: "+ from);
			}
			return;
		}
		
		// MSG , TELL commands  (many of my friends are ex-mmo players, adding these for ease)
		else if ((words[0].compareToIgnoreCase("msg")== 0) || (words[0].compareToIgnoreCase("tell")== 0) || (words[0].compareToIgnoreCase("t")==0)) {		
			boolean sent = false;
			ThreadHandler fromUser = threads.get(from.toLowerCase());
			if (threads.containsKey(words[1])) {
				threads.get(words[1].toLowerCase()).sendMessage("[PM] "+ from, words[2]);
				sent = true;
			}
			
			if ((fromUser != null) && sent) {
				fromUser.sendMessage("Sent PM["+ words[1] + "]", ": " + words[2]);
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
