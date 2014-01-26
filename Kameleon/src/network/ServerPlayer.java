package network;

import java.net.Socket;

public class ServerPlayer {
	private String name;
	private String textToSign;
	private String privateKey;
	private boolean isAuthenticated;
	
	private ServerRolitSocket srs;
	private PKISocket ps;
	
	public ServerPlayer(Socket inputSocket) {
		srs = new ServerRolitSocket(inputSocket);
		isAuthenticated = false;
	}
	
	public void start() {
		srs.start();
	}
	
	public ServerRolitSocket net() {
		return srs;
	}
	
	public PKISocket pki() {
		return ps;
	}
}
