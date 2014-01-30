package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerBouncer implements Runnable {
	private ServerSocket ssock;
	private boolean running;
	LinkedList<Socket> socks;
	private int port;
	
	public static final int START_NOTYET = 0;
	public static final int START_ERROR = 1;
	public static final int START_SUCCESS = 2;
	private int startedProperly = START_NOTYET;
	
	/**
	 * Constructs the Server Port Listener on given port.
	 * @param port - The port to listen on
	 */
	//@ requires inputPort != 0;
	public ServerBouncer(int inputPort) {
		running = false;
		port = inputPort;
	}
		
	/**
	 * The main loop of the listener. Pushes new connections on the stack
	 * and terminates as soon as close() is called.
	 */
	public void run() {
		try {
			ssock = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Network error: couldn't open server socket on port "
					+ Integer.toString(port));
			startedProperly = START_ERROR;
			return;
		}
		
		startedProperly = START_SUCCESS;
		
		Socket tSock;
		socks = new LinkedList<Socket>();
		running = true;
		
		while (running) {
			try {
				tSock = ssock.accept();
				
				synchronized (socks) {
					socks.add(tSock);
				}
			} catch (IOException e) {
				System.out.println("Network Error: something went wrong with a new socket?");
			}
		}
	}
	
	/**
	 * To check if there is a new connection waiting to be handled.
	 * @return - True if there is more than one connection
	 */
	//@ ensures !isRunning() ==> \result == false;
	//@ pure;
	public boolean isNewConnection() {
		if (socks == null) {
			return false;
		}
		
		synchronized (socks) {
			return !socks.isEmpty();
		}
	}
	
	/**
	 * Returns a socket that is waiting to be handled.
	 * @return - A socket
	 */
	//@ ensures !isRunning() ==> \result == null;
	//@ ensures isNewConnection() && isRunning() ==> \result != null;
	public Socket getNewConnection() {
		if (socks == null) {
			return null;
		}
		synchronized (socks) {
			if (!socks.isEmpty()) {
				return socks.removeFirst();
			} else {
				return null;
			}
		}
	}
	
	/**
	 * To check if the main loop of this thread is still running.
	 * @return True if the thread is running. Otherwhise false
	 */
	//@ pure;
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Terminates the thread and closes the server socket.
	 * Also closes any waiting connections.
	 */
	//@ requires isRunning();
	//@ ensures !isRunning();
	public void close() {
		try {
			ssock.close();
			running = false;
			synchronized (socks) {
				for (int i = 0; i < socks.size(); i++) {
					socks.get(i).close();
				}
			}
		} catch (IOException e) {
			System.out.println("Server Socket exception: exception while closing the socket");
		}
	}
	
	/**
	 * Returns false if the thread didn't start properly.
	 * @return True if it did start properly. Otherwise false.
	 */
	public int isStartedProperly() {
		return startedProperly;
	}
}
