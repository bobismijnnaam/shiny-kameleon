package network;

import java.io.IOException;
import java.util.Arrays;

import utility.Utils;
import network.RolitSocket.MessageType;

public class PerfectClient {
	public static void main(String[] args) throws IOException {
		String id = PKISocket.getRandomString(5);
		
		PKISocket pki = new PKISocket("test1", "test1");
		pki.start();
		
		while (!pki.isPrivateKeyReady()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		ClientRolitSocket crs = new ClientRolitSocket("localhost", Server.SERVER_PORT);
		crs.start();
		
		while (!crs.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}

		crs.askLOGIN("test1");
		System.out.println("Asking for login...");
		
		while (crs.getQueuedMsgType() != MessageType.AC_VSIGN) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		System.out.println("Received string to sign!");
		String toSign = crs.getQueuedMsg();
		System.out.println("[PKI] String to sign: " + toSign);
		System.out.println("[PKI] Private key: " + pki.getPrivateKey());
		crs.tellVSIGN(toSign, pki.getPrivateKey());
		
		System.out.println("Sent signed string");
		
		while (crs.getQueuedMsgType() != RolitSocket.MessageType.AC_HELLO) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		crs.getQueuedMsg();
		crs.tellHELLO();
		
		int startTime = (int) (System.currentTimeMillis() / 1000L);
		int nextMsgTime = (int) (startTime + Math.random() * 10 + 5);
		
		while ((int) (System.currentTimeMillis() / 1000L) - startTime < 100) {
			if (nextMsgTime < (int) (System.currentTimeMillis() / 1000L)) {
				crs.tellCHATM("HELLO WORLD! REGARDS " + id);
				nextMsgTime = (int) ((int) (System.currentTimeMillis() / 1000L)
						+ Math.random() * 10 + 5);
			}
			
			if (crs.getQueuedMsgType() == MessageType.AL_CHATM) {
				String[] msg = crs.getQueuedMsgArray();
				System.out.print("[" + msg[0] + "] ");
				msg = Arrays.copyOfRange(msg, 1, msg.length);
				System.out.println(Utils.join(Arrays.asList(msg), " "));
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
			
		crs.close();
	}
}
