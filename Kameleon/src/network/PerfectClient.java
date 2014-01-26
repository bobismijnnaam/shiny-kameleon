package network;

import network.RolitSocket.MessageType;

public class PerfectClient {
	public static void main(String[] args) {
		ClientRolitSocket crs = new ClientRolitSocket("localhost", Server.SERVER_PORT);
		crs.start();
		
		while (!crs.isConnected()) {
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
			} catch (Exception e) {
				System.out.println("Streams not yet loaded");
			}
		}
		
		System.out.println("Received string to sign!");
		String toSign = crs.getQueuedMsg();
		
		System.out.println(toSign);
	}
}
