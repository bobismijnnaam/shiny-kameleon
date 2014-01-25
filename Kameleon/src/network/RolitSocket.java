package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RolitSocket extends Thread {
	
	public enum MessageType {
		// The commands you can always do
		AL_STATE, // Gives the state of the player
		AL_SCORE, // Complex command!
		AL_BCAST, // Used by server to send message to player
		AL_ALIVE, // Try writing. If exception, then disconnected!
		AL_LEAVE, // From server means lobby leave, from client means exit
		AL_CHATM,
		
		// Authentication commands
		AC_LOGIN, // First call to server
		AC_VSIGN, // Response by server, client
		AC_HELLO, // Confirmation that you are who you say you are
		
		// Lobby commands
		LO_NGAME, // To start a new game
		LO_START, // Game is actually starting
		LO_PLIST,
		LO_LJOIN, // Indicate that a player joined the lobby
		LO_INVIT,
		
		// In game commands
		IG_GTURN, 
		IG_GMOVE,
		IG_GPLST, // Returns the players in the current game ()
		IG_BOARD, // Gets the current board
		
		// Feedback commands. Are always allowed!
		FB_ERROR, 
		FB_WARNI,
		FB_PROTO, // Without authentication!
		FB_SINFO, // Without authentication!
		
		X_NONE; // To indicate that there is no message
	} // 18 commands
	
	public enum Error {
		TooManyArgumentsException,
		TooFewArgumentsException,
		IllegalArgumentException,
		UnsupportedOperationException,
		UnexpectedOperationException,
		
		IllegalPlayerNameException,
		AuthenticationServerConnectionException,
		LogInFailedException,
		ServerFullException,
		
		InvalidLocationException,
		LocationTakenException,
		IllegalColorException,
		ConnectionLostException;
		
		private String s;
		
		static {
			TooManyArgumentsException.s = "TooManyArgumentsException";
			TooFewArgumentsException.s = "TooFewArgumentsException";
			IllegalArgumentException.s = "IllegalArgumentException";
			UnsupportedOperationException.s = "UnsupportedOperationException";
			UnexpectedOperationException.s = "UnexpectedOperationException";
			
			IllegalPlayerNameException.s = "IllegalPlayerNameException";
			AuthenticationServerConnectionException.s = "AuthenticationServerConnectionException";
			LogInFailedException.s = "LogInFailedException";
			ServerFullException.s = "ServerFullException";
			
			InvalidLocationException.s = "InvalidLocationException";
			LocationTakenException.s = "LocationTakenException";
			IllegalColorException.s = "IllegalColorException";
			ConnectionLostException.s = "ConnectionLostException";
		}
		
		public String toString() {
			return s;
		}
	}
	
	public final static boolean ID_SERVER = true;
	public final static boolean ID_CLIENT = false;
	
	ServerSocket serversock;
	private Socket sock;
	private boolean id;
	private boolean connected = false;
	private boolean running = false;
	
	String serverAddress;
	int serverPort;
	
	private BufferedReader in;
	private BufferedWriter out;
	
	private Lock listLock;
	private List<String[]> queuedMsgs;
	private List<MessageType> queuedMsgsType;
	
	// For the server
	public RolitSocket(int port) {
		id = ID_SERVER;
		
		// Try to connect to a player
		try {
			// Start listening on a port
			serversock = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not open socket on port " + Integer.toString(port));
			System.exit(0);
		}
	}
	
	// For the client
	public RolitSocket(String addr, int port) {
		id = ID_CLIENT;
		
		serverAddress = addr;
		serverPort = port;
	}
	
	public String toString() {
		if (id == ID_SERVER) {
			return "Server";
		} else {
			return "Client";
		}
	}
	
	public boolean getType() {
		return id;
	}
	
	public String getOtherType() {
		if (getType() == ID_SERVER) {
			return "Client";
		} else {
			return "Server";
		}
	}
	
	public void setupSocket() {
		if (id == ID_SERVER) {
			// Connect with a player
			try {
				// System.out.println("Looking for a player...");
				sock = serversock.accept();
				// System.out.println("Found a player!");
				
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			} catch (IOException e) {
				System.out.println("Network error: could not open a socket with client.");
				System.exit(0);
			}
		} else {
			// Connect with a server
			try {
				// System.out.println("Looking for a server...");
				sock = new Socket(serverAddress, serverPort);
				// System.out.println("Found a server!");
				
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			} catch (IOException e) {
				System.out.println("ERROR: could not create a socket on " + serverAddress
						+ " and port " + serverPort);
				System.exit(0);
			}
		}
	}
	
	public void processCommand(String token, String[] tail) {
		// Parse msg prefix and the rest of the message
		// Commands that can be invoked at any given point in time
		if (token.equals("PROTO")) { // For protocol type info
			if (tail == null) {
				tellPROTO();
			} else if (tail.length < 2) {
				tellERROR(Error.TooFewArgumentsException);
			} else if (tail.length > 2) {
				tellERROR(Error.TooManyArgumentsException);
			} else {
				// parse prototype
				queueMsg(MessageType.FB_PROTO, tail);
			}
		} else if (token.equals("STATE")) { // For the state of the player
			if (id == ID_SERVER) {
				// Queue the message, so the outer class can send
				// the state using a function of this class
				if (tail != null) {
					tellERROR(Error.TooManyArgumentsException);
				} else {
					queueMsg(MessageType.AL_STATE);
				}
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length > 1) {
					tellERROR(Error.TooManyArgumentsException);
				} else {
					queueMsg(MessageType.AL_STATE, tail);
				}
			}
		} else if (token.equals("BCAST")) { // A message from the server
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException);
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else {
					queueMsg(MessageType.AL_BCAST, tail);
				}
			}
		} else if (token.equals("SINFO")) { // For name and version number
			if (tail == null) {
				tellSINFO();
			} else if (tail.length == 1) {
				tellERROR(Error.TooFewArgumentsException);
			} else if (tail.length > 2) {
				tellERROR(Error.TooManyArgumentsException);
			} else {
				queueMsg(MessageType.FB_SINFO, tail);
			}
		} else if (token.equals("ERROR")) { // For an error of the other side
			// TODO: Maybe write details to error.log?
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException);
			} else {
				queueMsg(MessageType.FB_ERROR, tail);
			}
		} else if (token.equals("WARNI")) { // For a warning from the other side
			// TODO: Maybe write details to warning.log?
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException);
			} else {
				String warning = Arrays.toString(tail);
				System.out.println("Received a warning from other side: \""
						+ warning + "\"");
			}
		} else if (token.equals("SCORE")) { // A score request
			if (tail == null || tail.length == 1) {
				tellERROR(Error.TooFewArgumentsException);
			} else {
				queueMsg(MessageType.FB_ERROR, tail);
			}
		} else if (token.equals("LEAVE")) { // When a client disconnects or leaves the lobby
			if (id == ID_SERVER) { // Disconnect happened
				if (tail == null) {
					queueMsg(MessageType.AL_LEAVE);
				} else { // More than 0 arguments is unexpected!
					tellERROR(Error.IllegalArgumentException);
				}
			} else { // Someone left the lobby
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 1) {
					queueMsg(MessageType.AL_LEAVE, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
				
			}
		} else if (token.equals("LOGIN")) { // When a client tries to get access to the server
			if (id == ID_SERVER) { // Someone's trying to connect
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 1) {
					queueMsg(MessageType.AC_LOGIN, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			} else { // Client does not support this.
				tellERROR(Error.UnexpectedOperationException);
			}
		} else if (token.equals("VSIGN")) { // To prove a clients identity
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException);
			} else if (tail.length > 1) {
				tellERROR(Error.TooManyArgumentsException);
			} else {
				queueMsg(MessageType.AC_VSIGN, tail);
			}
		} else if (token.equals("HELLO")) {
			// To acknowledge a clients identity
			// and tell client/server your capabilities
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException);
			} else if (tail.length > 1) {
				tellERROR(Error.TooManyArgumentsException);
			} else {
				queueMsg(MessageType.AC_HELLO, tail);
			}
		} else if (token.equals("NGAME")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 1 || tail.length == 2) {
					queueMsg(MessageType.LO_NGAME);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			} else {
				tellERROR(Error.UnexpectedOperationException);
			}
		} else if (token.equals("START")) {
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException);
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length > 4) {
					tellERROR(Error.TooManyArgumentsException);
				} else {
					queueMsg(MessageType.LO_START, tail);
				}
			}
		} else if (token.equals("GTURN")) {
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException);
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length > 1) {
					tellERROR(Error.TooManyArgumentsException);
				} else {
					queueMsg(MessageType.IG_GTURN, tail);
				}
			}
		} else if (token.equals("GMOVE")) {
			if (id == ID_SERVER) {
				if (tail == null || tail.length == 1) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 2) {
					queueMsg(MessageType.IG_GMOVE, tail);
				} else if (tail.length > 2) {
					tellERROR(Error.TooManyArgumentsException);
				}
			} else {
				if (tail == null || tail.length < 3) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 3) {
					queueMsg(MessageType.IG_GMOVE, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			}
		} else if (token.equals("BOARD")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					queueMsg(MessageType.IG_BOARD);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			} else { 
				if (tail == null || tail.length < 64) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 64) {
					queueMsg(MessageType.IG_BOARD, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			}
		} else if (token.equals("GPLST")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					queueMsg(MessageType.IG_GPLST);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			} else {
				if (tail == null || tail.length == 1) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length <= 4) {
					queueMsg(MessageType.IG_GPLST, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			}
		} else if (token.equals("ALIVE")) {
			return;
		} else if (token.equals("PLIST")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					queueMsg(MessageType.LO_PLIST);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			} else {
				queueMsg(MessageType.LO_PLIST, tail);
			}
		} else if (token.equals("LJOIN")) {
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException);
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException);
				} else if (tail.length == 1) {
					queueMsg(MessageType.LO_LJOIN, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException);
				}
			}
		} else if (token.equals("INVIT")) {
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException);
			} else if (tail.length > 5) {
				tellERROR(Error.TooManyArgumentsException);
			} else {
				queueMsg(MessageType.LO_INVIT, tail);
			}
		} else if (token.equals("CHATM")) {
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException);
			} else {
				queueMsg(MessageType.AL_CHATM);
			}
		} else {
			tellERROR(Error.UnsupportedOperationException);
		}
	}
	
	
	public void run() {
		// Initialize lock
		listLock = new ReentrantLock();
		
		// Establish connection
		setupSocket();
		
		System.out.println("Connected [" + getType() + "]");
		
		queuedMsgs = Collections.synchronizedList(new ArrayList<String[]>());
		queuedMsgsType = Collections.synchronizedList(new ArrayList<MessageType>());
		
		running = true;
		connected = true;
		
		while (running) {
			try {
				if (in.ready()) {
					// Download the string
					String msg;
					try {
						msg = in.readLine();
					} catch (IOException e1) {
						msg = null;
						System.out.println("Network error: could not read from socket inputstream");
						break;
					}
					
					// Pre-parse the string
					String[] msgSplit = msg.split(" ");
					String token = msgSplit[0];
					String[] tail = null;
					if (msgSplit.length > 1) {
						tail = Arrays.copyOfRange(msgSplit, 1, msgSplit.length);
					} 
					
					// Process the command
					processCommand(token, tail);
				}
			} catch (IOException e1) {
				System.out.println("Network error: could not curry socket! Closing socket.");
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep!");
			}
		}
		
		System.out.println("Closing RolitSocket [" + getType() + "]");
		
		// Tell the other side we are disconnecting. If we are a client
		// a LEAVE command will be dispatched. Otherwise, nothing will
		// be sent.
		if (getType() == ID_CLIENT) {
			sendMsg("LEAVE");
		}
		
		try {
			sock.close();
			System.out.println(getType() + " socket closed.");
			if (serversock != null) {
				serversock.close();
				System.out.println("Special server socket closed;");
			}
			
			connected = false;
		} catch (IOException e) {
			// If this happens there will be some serious issues.
			e.printStackTrace();
		}
	}
	
	public void close() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isNewMsgQueued() {
		listLock.lock();
		int size = queuedMsgs.size(); 
		listLock.unlock();
		return size > 0;
	}
	
	public MessageType getQueuedMsgType() {
		listLock.lock();
		if (queuedMsgsType.size() > 0) {
			MessageType type = queuedMsgsType.get(0);
			listLock.unlock();
			return type;
		} else {
			listLock.unlock();
			return MessageType.X_NONE;
		}
	}
	
	public String[] getQueuedMsgArray() {
		listLock.lock();
		
		if (queuedMsgsType.size() > 0) {
			queuedMsgsType.remove(0);
			String[] msg = queuedMsgs.remove(0);
			
			listLock.unlock();
			return msg;
		} else {
			listLock.unlock();
			return null;
		}
	}
	
	public String getQueuedMsg() {
		String[] args = getQueuedMsgArray();
		String result = new String();
		for (int i = 0; i < args.length - 1; i++) {
			result += args[i] + " ";
		}
		
		result += args[args.length - 1];
		
		return result;
	}
	
	private void queueMsg(MessageType type) {
		queueMsg(type, new String[0]);
	}
	
	private void queueMsg(MessageType type, String[] msg) {
		listLock.lock();
		queuedMsgsType.add(type);
		queuedMsgs.add(msg);
		listLock.unlock();
	}
	
	/**
	 * Send an arbitrary string to the other side. Adds a newline for you if needed.
	 * @param msg - The message to be sent.
	 */
	protected void sendMsg(String msg) {
		try {
			if (msg.substring(msg.length() - 1).equals("\n")) {
				out.write(msg);
			} else {
				out.write(msg + "\n");
			}
			out.flush();
		} catch (IOException e) {
			// Does this mean that the other side disconnected?
			System.out.println("Network error: couldn't send to the "
					+ getType()
					+ " . The command was: \"" + msg + "\"");
		}
	}
	
//	private void oocWarning(String func) { // Out Of Context warning 
//		System.out.println("RolitSocket warning: " + func + " is being "
//				+ "used out of context. Only the " + getOtherType()
//				+ " is allowed to use this function.");
//	}
	
	/////////////////////////////////////
	/////////////////////////////////////
	///////// COMMAND FUNCTIONS /////////
	/////////////////////////////////////
	/////////////////////////////////////
	
	public void tellPROTO() {
		String msg = "PROTO INFB 1.3.1";
		sendMsg(msg);
	}
	
	public void askPROTO() {
		String msg = "PROTO";
		sendMsg(msg);
	}
	
	public void tellSINFO() {
		if (id == ID_SERVER) {
			sendMsg("SINFO HONEYBADGER 0.0.1");
		} else {
			sendMsg("SINFO CARRION 0.0.1");
		}
	}
	
	public void askSINFO() {
		sendMsg("SINFO");
	}
	
	public void tellERROR(Error e) {
		sendMsg(e.toString());
	}
	
	public void tellHELLO() {
		sendMsg("HELLO CL");
	}
}
