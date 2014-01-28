package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import utility.Utils;

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
		
		private String s;
		
		static {
			// The commands you can always do
			AL_STATE.s = "AL_STATE";
			AL_SCORE.s = "AL_SCORE";
			AL_BCAST.s = "AL_BCAST";
			AL_ALIVE.s = "AL_ALIVE";
			AL_LEAVE.s = "AL_LEAVE";
			AL_CHATM.s = "AL_CHATM";
			
			// Authentication commands
			AC_LOGIN.s = "AC_LOGIN";
			AC_VSIGN.s = "AC_VSIGN";
			AC_HELLO.s = "AC_HELLO";
			
			// Lobby commands
			LO_NGAME.s = "LO_NGAME";
			LO_START.s = "LO_START";
			LO_PLIST.s = "LO_PLIST";
			LO_LJOIN.s = "LO_LJOIN";
			LO_INVIT.s = "LO_INVIT";
			
			// In game commands
			IG_GTURN.s = "IG_GTURN";
			IG_GMOVE.s = "IG_GMOVE";
			IG_GPLST.s = "IG_GPLST";
			IG_BOARD.s = "IG_BOARD";
			
			// Feedback commands. Are always allowed!
			FB_ERROR.s = "FB_ERROR";
			FB_WARNI.s = "FB_WARNI";
			FB_PROTO.s = "FB_PROTO";
			FB_SINFO.s = "FB_SINFO";
			
			X_NONE.s = "X_NONE";
		}
		
		public String toString() {
			return new String(s);
		}
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
	
//	ServerSocket serversock;
	private Socket sock;
	private boolean id;
	private boolean connected = false;
	private boolean running = false;
	private boolean closeCalled = false;
	
	String serverAddress;
	int serverPort;
	
	private BufferedReader in;
	private BufferedWriter out;
	
	private Lock listLock;
	private List<String[]> queuedMsgs;
	private List<MessageType> queuedMsgsType;
	
	// For the server
	public RolitSocket(Socket inputSock) {
		id = ID_SERVER;
		
		sock = inputSock;
		
//		// Try to connect to a player
//		try {
//			// Start listening on a port
//			serversock = new ServerSocket(port);
//		} catch (IOException e) {
//			System.out.println("Could not open socket on port " + Integer.toString(port));
//			System.exit(0);
//		}
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
				// sock = serversock.accept();
				// System.out.println("Found a player!");
				
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			} catch (IOException e) {
				System.out.println("Network error: could not open the socket streams.");
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
	
	public void processCommand(String token, String[] tail) throws IOException {
		// Parse msg prefix and the rest of the message
		// Commands that can be invoked at any given point in time
		if (token.equals("PROTO")) { // For protocol type info
			if (tail == null) {
				tellPROTO();
			} else if (tail.length < 2) {
				tellERROR(Error.TooFewArgumentsException, "PROTO");
			} else if (tail.length > 2) {
				tellERROR(Error.TooManyArgumentsException, "PROTO");
			} else {
				// parse prototype
				queueMsg(MessageType.FB_PROTO, tail);
			}
		} else if (token.equals("STATE")) { // For the state of the player
			if (id == ID_SERVER) {
				// Queue the message, so the outer class can send
				// the state using a function of this class
				if (tail != null) {
					tellERROR(Error.TooManyArgumentsException, "STATE");
				} else {
					queueMsg(MessageType.AL_STATE);
				}
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "STATE");
				} else if (tail.length > 1) {
					tellERROR(Error.TooManyArgumentsException, "STATE");
				} else {
					queueMsg(MessageType.AL_STATE, tail);
				}
			}
		} else if (token.equals("BCAST")) { // A message from the server
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException, "BCAST");
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "BCAST");
				} else {
					queueMsg(MessageType.AL_BCAST, tail);
				}
			}
		} else if (token.equals("SINFO")) { // For name and version number
			if (tail == null) {
				tellSINFO();
			} else if (tail.length == 1) {
				tellERROR(Error.TooFewArgumentsException, "SINFO");
			} else if (tail.length > 2) {
				tellERROR(Error.TooManyArgumentsException, "SINFO");
			} else {
				queueMsg(MessageType.FB_SINFO, tail);
			}
		} else if (token.equals("ERROR")) { // For an error of the other side
			// TODO: Maybe write details to error.log?
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException, "ERROR (lol)");
			} else {
				queueMsg(MessageType.FB_ERROR, tail);
			}
		} else if (token.equals("WARNI")) { // For a warning from the other side
			// TODO: Maybe write details to warning.log?
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException, "WARNI (lel)");
			} else {
				String warning = Arrays.toString(tail);
				System.out.println("Received a warning from other side: \""
						+ warning + "\"");
			}
		} else if (token.equals("SCORE")) { // A score request
//			if (tail == null || tail.length == 1) { // What if there are no highscores?
//				tellERROR(Error.TooFewArgumentsException);
//			} else {
			queueMsg(MessageType.AL_SCORE, tail);
//			}
		} else if (token.equals("LEAVE")) { // When a client disconnects or leaves the lobby
			if (id == ID_SERVER) { // Disconnect happened
				if (tail == null) {
					queueMsg(MessageType.AL_LEAVE);
				} else { // More than 0 arguments is unexpected!
					tellERROR(Error.IllegalArgumentException, "LEAVE");
				}
			} else { // Someone left the lobby
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "TAIL");
				} else if (tail.length == 1) {
					queueMsg(MessageType.AL_LEAVE, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException, "TAIL");
				}
				
			}
		} else if (token.equals("LOGIN")) { // When a client tries to get access to the server
			if (id == ID_SERVER) { // Someone's trying to connect
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "LOGIN");
				} else if (tail.length == 1) {
					queueMsg(MessageType.AC_LOGIN, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException, "LOGIN");
				}
			} else { // Client does not support this.
				tellERROR(Error.UnexpectedOperationException, "LOGIN");
			}
		} else if (token.equals("VSIGN")) { // To prove a clients identity
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException, "VSIGN");
			} else if (tail.length > 1) {
				tellERROR(Error.TooManyArgumentsException, "VSIGN");
			} else {
				queueMsg(MessageType.AC_VSIGN, tail);
			}
		} else if (token.equals("HELLO")) {
			// To acknowledge a clients identity
			// and tell client/server your capabilities
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException, "VSIGN");
			} else if (tail.length > 1) {
				tellERROR(Error.TooManyArgumentsException, "VSIGN");
			} else {
				queueMsg(MessageType.AC_HELLO, tail);
			}
		} else if (token.equals("NGAME")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "NGAME");
				} else if (tail.length == 1 || tail.length == 2) {
					queueMsg(MessageType.LO_NGAME);
				} else {
					tellERROR(Error.TooManyArgumentsException, "NGAME");
				}
			} else {
				tellERROR(Error.UnexpectedOperationException, "NGAME");
			}
		} else if (token.equals("START")) {
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException, "START");
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "START");
				} else if (tail.length > 4) {
					tellERROR(Error.TooManyArgumentsException, "START");
				} else {
					queueMsg(MessageType.LO_START, tail);
				}
			}
		} else if (token.equals("GTURN")) {
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException, "GTURN");
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "GTURN");
				} else if (tail.length > 1) {
					tellERROR(Error.TooManyArgumentsException, "GTURN");
				} else {
					queueMsg(MessageType.IG_GTURN, tail);
				}
			}
		} else if (token.equals("GMOVE")) {
			if (id == ID_SERVER) {
				if (tail == null || tail.length == 1) {
					tellERROR(Error.TooFewArgumentsException, "GMOVE");
				} else if (tail.length == 2) {
					queueMsg(MessageType.IG_GMOVE, tail);
				} else if (tail.length > 2) {
					tellERROR(Error.TooManyArgumentsException, "GMOVE");
				}
			} else {
				if (tail == null || tail.length < 3) {
					tellERROR(Error.TooFewArgumentsException, "GMOVE");
				} else if (tail.length == 3) {
					queueMsg(MessageType.IG_GMOVE, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException, "GMOVE");
				}
			}
		} else if (token.equals("BOARD")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					queueMsg(MessageType.IG_BOARD);
				} else {
					tellERROR(Error.TooManyArgumentsException, "BOARD");
				}
			} else { 
				if (tail == null || tail.length < 64) {
					tellERROR(Error.TooFewArgumentsException, "BOARD");
				} else if (tail.length == 64) {
					queueMsg(MessageType.IG_BOARD, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException, "BOARD");
				}
			}
		} else if (token.equals("GPLST")) {
			if (id == ID_SERVER) {
				if (tail == null) {
					queueMsg(MessageType.IG_GPLST);
				} else {
					tellERROR(Error.TooManyArgumentsException, "GPLST");
				}
			} else {
				if (tail == null || tail.length == 1) {
					tellERROR(Error.TooFewArgumentsException, "GPLST");
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
					tellERROR(Error.TooManyArgumentsException, "PLIST");
				}
			} else {
				queueMsg(MessageType.LO_PLIST, tail);
			}
		} else if (token.equals("LJOIN")) {
			if (id == ID_SERVER) {
				tellERROR(Error.UnexpectedOperationException, "LJOIN");
			} else {
				if (tail == null) {
					tellERROR(Error.TooFewArgumentsException, "LJOIN");
				} else if (tail.length == 1) {
					queueMsg(MessageType.LO_LJOIN, tail);
				} else {
					tellERROR(Error.TooManyArgumentsException, "LJOIN");
				}
			}
		} else if (token.equals("INVIT")) {
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException, "INVIT");
			} else if (tail.length > 5) {
				tellERROR(Error.TooManyArgumentsException, "INVIT");
			} else {
				queueMsg(MessageType.LO_INVIT, tail);
			}
		} else if (token.equals("CHATM")) {
			if (tail == null) {
				tellERROR(Error.TooFewArgumentsException, "CHATM");
			} else {
				queueMsg(MessageType.AL_CHATM, tail);
			}
		} else {
			tellERROR(Error.UnsupportedOperationException, "CHATM");
		}
	}
	
	public void run() {
		// Initialize lock
		listLock = new ReentrantLock();
		
		// Establish connection
		setupSocket();
		
		System.out.println("[" + toString() + "Socket] Connected");
		
		queuedMsgs = Collections.synchronizedList(new ArrayList<String[]>());
		queuedMsgsType = Collections.synchronizedList(new ArrayList<MessageType>());
		
		running = true;
		connected = true;
		
		while (running) {
			try {
				if (in.ready()) {
					// Download the string
					String msg = in.readLine();
					
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
				close();
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep!");
			}
		}
		
		System.out.println("Closing RolitSocket [" + toString() + "]");
		
		// Tell the other side we are disconnecting. If we are a client
		// a LEAVE command will be dispatched. Otherwise, nothing will
		// be sent.
		if (getType() == ID_CLIENT) {
			sendMsg("LEAVE");
		}
		
		try {
			sock.close();
			System.out.println(toString() + " socket closed.");
//			if (serversock != null) {
//				serversock.close();
//				System.out.println("Special server socket closed;");
//			}
			
			connected = false;
		} catch (IOException e) {
			// If this happens there will be some serious issues.
			e.printStackTrace();
		}
		
		// Nullify the streams to make sure they don't cause errors when writing
		in = null;
		out = null;
	}
	
	public void close() {
		running = false;
		closeCalled = true;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isCloseCalled() {
		return closeCalled;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isNewMsgQueued() {
		if (queuedMsgs == null) {
//			throw new IOException("Socket Exception: socket has not yet connected");
			return false;
		}
		
		listLock.lock();
		int size = queuedMsgs.size(); 
		listLock.unlock();
		return size > 0;
	}
	
	public MessageType getQueuedMsgType() {
		if (queuedMsgs == null) {
//			throw new IOException("Socket Exception: socket has not yet connected");
			return MessageType.X_NONE;
		}
		
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
		String[] msg = getQueuedMsgArray();
		if (msg == null) {
			return null;
		} else {
			return Utils.join(Arrays.asList(msg), " ");
		}
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
	
	// TODO 
	/**
	 * Send an arbitrary string to the other side. Adds a newline for you if needed.
	 * @param msg - The message to be sent.
	 * @throws StreamNotOpenException 
	 */
	protected void sendMsg(String msg) {
		if (out == null) {
//			throw new StreamNotOpenException("Socket Exception: Socket stream not yet opened");
			return; // Function is not supposed to be used before the streams are ready!
		}
		
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
					+ getOtherType()
					+ " . The command was: \"" + msg + "\"");
			close();
		}
	}
	
	/////////////////////////////////////
	/////////////////////////////////////
	///////// COMMAND FUNCTIONS /////////
	/////////////////////////////////////
	/////////////////////////////////////
	
	public void tellPROTO() {
		String msg = "PROTO INFB 1.4.0";
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
		sendMsg("ERROR " + e.toString());
	}
	
	public void tellERROR(Error e, String details) {
		sendMsg("ERROR " + e.toString() + " " + details);
	}
	
	public void tellWARNI(String info) {
		sendMsg("WARNI " + info);
	}
	
	public void tellHELLO() {
		sendMsg("HELLO CL");
	}
	
	public boolean askALIVE() {
		try {
			out.write("ALIVE");
			out.flush();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
}
