package network;

import java.io.IOException;

import network.RolitSocket.MessageType;

public class PerfectClient {
	public static void main(String[] args) throws IOException {
		ClientRolitSocket crs = new ClientRolitSocket("localhost", Server.SERVER_PORT);
		crs.start();
		
		while (!crs.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		System.out.println("Asking for login...");
		crs.askLOGIN("player_test1");
		
		MessageType newMsgType = MessageType.X_NONE;
		
		while (newMsgType != MessageType.AC_VSIGN) {
			try {
				newMsgType = crs.getQueuedMsgType();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		System.out.println("Received string to sign!");
		String toSign = crs.getQueuedMsg();
		
		PKISocket pki = new PKISocket("player_test1", "test1");
		pki.start();
		
		while (!pki.isPrivateKeyReady()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		crs.tellVSIGN(toSign, pki.getPrivateKey());
		
		System.out.println(toSign);
	}
}
