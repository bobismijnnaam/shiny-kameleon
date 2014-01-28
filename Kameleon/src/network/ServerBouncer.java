package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerBouncer implements Runnable {
	private ServerSocket ssock;
	private boolean running;
	LinkedList<Socket> socks;
	
	public ServerBouncer(int port) {
		running = false;
	}
		
	public void run() {
		try {
			ssock = new ServerSocket(Server.SERVER_PORT);
		} catch (IOException e) {
			System.out.println("Network error: couldn't open server socket on port "
					+ Integer.toString(Server.SERVER_PORT));
		}
		
		running = true;
		Socket tSock;
		socks = new LinkedList<Socket>();
		
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
	
	public boolean isNewConnection() {
		if (socks == null) {
			return false;
		}
		
		synchronized (socks) {
			return !socks.isEmpty();
		}
	}
	
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
	
	public boolean isRunning() {
		return running;
	}
	
	public void close() {
		try {
			ssock.close();
			running = false;
		} catch (IOException e) {
			System.out.println("Server Socket exception: exception while closing the socket");
		}
	}
}
