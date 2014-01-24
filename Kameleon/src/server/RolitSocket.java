package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RolitSocket extends Thread {
	
	public enum MessageType {
		// The commands you can always do
		AL_STATE, // Gives the state of the player
		AL_SCORE, // Complex command!
		AL_BCAST, // Used by server to send message to player
		AL_ALIVE, // Try writing. If exception, then disconnected!
		
		// Authentication commands
		AC_LOGIN, // First call to server
		AC_VSIGN, // Response by server, client
		AC_HELLO, // Confirmation that you are who you say you are
		
		// Lobby commands
		LO_NGAME, // To start a new game
		LO_START, // Game is actually starting
		LO_BOARD, // Gets the current board
		LO_PLIST,
		
		// In game commands
		IG_GTURN, 
		IG_GMOVE,
		IG_GPLST, // Returns the players in the current game ()
		
		// Feedback commands. Are always allowed!
		FB_ERROR, 
		FB_WARNI,
		FB_PROTO, // Without authentication!
		FB_SINFO, // Without authentication!
		
		X_NONE; // To indicate that there is no message
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
	
	private Scanner in;
	private BufferedWriter out;
	
	// private String queuedMsg;
	private List<String> queuedMsgs;
	// private MessageType currentMsgType;
	private List<MessageType> queuedMsgsType;
	
	// For the server
	public RolitSocket(int port) {
		id = ID_SERVER;
		
		// Try to connect to a player
		try {
			// Start listening on a port
			serversock = new ServerSocket(port);
			System.out.println("Trying to open a connection...");
			
			// Start the process & listening
			start();
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
		
		start();
	}
	
	public void run() {
		// Establish connection
		if (id == ID_SERVER) {
			// Connect with a player
			try {
				System.out.println("Looking for a player...");
				sock = serversock.accept();
				System.out.println("Found a player!");
				
				in = new Scanner(new InputStreamReader(sock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			} catch (IOException e) {
				System.out.println("Network error: could not open a socket with client.");
				System.exit(0);
			}
		} else {
			// Connect with a server
			try {
				System.out.println("Looking for a server...");
				sock = new Socket(serverAddress, serverPort);
				System.out.println("Found a server!");
				
				in = new Scanner(new InputStreamReader(sock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			} catch (IOException e) {
				System.out.println("ERROR: could not create a socket on " + serverAddress
						+ " and port " + serverPort);
				System.exit(0);
			}
		}
		
		queuedMsgs = Collections.synchronizedList(new ArrayList<String>());
		queuedMsgsType = Collections.synchronizedList(new ArrayList<MessageType>());
		
		running = true;
		connected = true;
		
		while (running) {
			String token = null;
			if (in.hasNext()) {
				// Read message prefix
				token = in.next();
				
				// Parse msg prefix and the rest of the message
				if (token.equals("PROTO")) {
					String tail = in.nextLine().trim();
					if (tail.length() > 0) {
						// parse prototype
						queueMsg(MessageType.FB_PROTO, tail);
					} else {
						tellProto();
					}
				}
			}
		}
		
		// TODO Send exit message if id == server?
		// Because if the while loop terminates we're obviously quitting
		
		try {
			sock.close();
			if (serversock != null) {
				serversock.close();
			}
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
		return queuedMsgs.size() > 0;
	}
	
	public MessageType getQueuedMsgType() {
		if (queuedMsgsType.size() > 0) {
			return queuedMsgsType.get(0);
		} else {
			return MessageType.X_NONE;
		}
	}
	
	public String getQueuedMsg() {
		if (queuedMsgsType.size() > 0) {
			queuedMsgsType.remove(0);
			return queuedMsgs.remove(0);
		} else {
			return null;
		}
	}
	
	/**
	 * Send an arbitrary string to the other side. Adds a newline for you!
	 * @param msg - The message to be sent.
	 */
	private void sendMsg(String msg) {
		try {
			out.write(msg + "\n");
			out.flush();
		} catch (IOException e) {
			// Does this mean that the other side disconnected?
			System.out.println("Network error: couldn't send to the "
					+ (id == ID_SERVER ? "server" : "client")
					+ " . The command was: \"" + msg + "\"");
		}
	}
	
	private void queueMsg(MessageType type, String msg) {
		queuedMsgsType.add(type);
		queuedMsgs.add(msg);
	}
	
	public void tellProto() {
		String msg = "PROTO INFB 1.3.0\n";
		sendMsg(msg);
	}
	
	public void askProto() {
		String msg = "PROTO\n";
		try {
			out.write(msg);
			out.flush();
		} catch (IOException e) {
			System.out.println("Network error: couldn't send PROTO to other side");
		}
	}

}
