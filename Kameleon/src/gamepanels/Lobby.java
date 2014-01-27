package gamepanels;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import network.ClientRolitSocket;
import network.PKISocket;
import network.Server;
import network.RolitSocket.MessageType;

public class Lobby extends JPanel {
	
	private String[] settings;
	
	public Lobby(Game inputGame, String[] inputSettings) {
		settings = inputSettings;
		createLobby(inputGame);
	}
	
	public void createLobby(Game inputGame) {
		JLabel test = new JLabel("Welcome to the lobby!");
		add(test);
		try {
			authenticate();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void authenticate() throws InterruptedException {
		PKISocket pki = new PKISocket(settings[0], settings[1]);
		pki.start();
		
		while (!pki.isPrivateKeyReady()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		ClientRolitSocket crs = new ClientRolitSocket(settings[2], Integer.parseInt(settings[3]));
		crs.start();
		
		while (!crs.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
		
		System.out.println("Asking for login...");
		try {
			crs.askLOGIN(settings[0]);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MessageType newMsgType = MessageType.X_NONE;
		
		while (newMsgType != MessageType.AC_VSIGN) {
			try {
				newMsgType = crs.getQueuedMsgType();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Frustatie");
			}
		}
		
		System.out.println("Received string to sign!");
		String toSign = crs.getQueuedMsg();
		
		try {
			crs.tellVSIGN(toSign, pki.getPrivateKey());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(toSign);
		
		System.out.println("Waiting for bob to implement hello..");
		while (newMsgType != MessageType.AC_HELLO) {
			try {
				newMsgType = crs.getQueuedMsgType();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Frustatie");
			}
		}
		
		System.out.println("Received Hello!");
		String hello = crs.getQueuedMsg();
		
		
	}
	
}
