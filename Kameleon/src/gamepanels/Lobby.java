package gamepanels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import utility.Utils;
import net.miginfocom.swing.MigLayout;
import network.ClientRolitSocket;
import network.PKISocket;
import network.Server;
import network.RolitSocket.MessageType;

public class Lobby extends JPanel implements ActionListener {
	
	private String[] settings;
	private JTextArea message;
	private ClientRolitSocket crs;
	MessageType newMsgType = MessageType.X_NONE;
	private JTextArea chat;
	private JTextArea playerName;
	private JButton c, d, h, i, j, send;
	public Lobby(Game inputGame, String[] inputSettings) {
		settings = inputSettings;
		createLobby(inputGame);
	}
	
	public void createLobby(Game inputGame) {
		JLabel test = new JLabel("Welcome to the lobby!");
		add(test);
		try {
			authenticate();
			init();
			SocketHandlerThread socketHandler = new SocketHandlerThread(crs);
			socketHandler.start();
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
		
		crs = new ClientRolitSocket(settings[2], Integer.parseInt(settings[3]));
		crs.start();
		
		while (!crs.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("fok joe");
			}
		}
	
		System.out.println("Asking for login...");
		crs.askLOGIN(settings[0]);
		
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
		crs.tellVSIGN(toSign, pki.getPrivateKey());
		
		System.out.println(toSign);
		
		// end of authentication
		
	}
	
	public void init() {
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
		
		// say hello back to server
		crs.tellHELLO();
		
		hello = hello.trim();
		System.out.println(hello);
		if (hello.equals("D")) {
			System.out.println("Default");
			drawDefault();
		} else if (hello.equals("DC")) {
			System.out.println("Default + chat");
			drawDefault();
			drawChat();
			addChatMessage("Bob", "Kom rolit spelen dan");
			addChatMessage("Florian", "nee fuck you");
		} else if (hello.equals("CL")) {
			drawDefault();
			drawChat();
		}
	}
	
	public void drawDefault() {
		JLabel title = new JLabel("Welcome to the default online game " + settings[0]);
		add(title);
		playerName = new JTextArea();
		playerName.setText("Playername");
		add(playerName);
		c = new JButton("Play 1 vs 1, enter player name");
		c.addActionListener(this);
		c.setName("NGAMEC");
		d = new JButton("Play 1 vs 1, default");
		d.addActionListener(this);
		d.setName("NGAMED");
		h = new JButton("Play against 1 player");
		h.addActionListener(this);
		h.setName("NGAMEH");
		i = new JButton("Play against 2 players");
		i.addActionListener(this);
		i.setName("NGAMEI");
		j = new JButton("Play against 3 players");
		j.addActionListener(this);
		j.setName("NGAMEJ");
		add(c);
		add(d);
		add(h);
		add(i);
		add(j);
	}
	
	public void drawLobby() {
		JLabel title = new JLabel("Welcome to the lobby " + settings[0]);
		add(title);
	}
	
	public void drawChat() {
		JPanel chatBox = new JPanel();
		chatBox.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		chatBox.setSize(400, 400);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		chat = new JTextArea("hii, i'm bob welcome to our fabulous chatbox!");
		chat.setBorder(BorderFactory.createLineBorder(Color.black));
		chat.setEditable(false);  
		chatBox.add(chat, gbc);
		message = new JTextArea("Message:");
		message.setBorder(BorderFactory.createLineBorder(Color.black));
		send = new JButton("Send");
		send.setName("send");
		gbc.gridy = 1;
		chatBox.add(message, gbc);
		gbc.gridy = 1;
		gbc.gridx = 1;
		chatBox.add(send, gbc);
		send.addActionListener(this);
		chatBox.setSize(400, 400);
		add(chatBox);
	}
	
	public void addChatMessage(String inputUsername, String inputMessage) {
		chat.append("\n" + inputUsername + ": " + inputMessage);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(send)) {
			System.out.println(message.getText());
			crs.tellCHATM(message.getText());
			System.out.println("Send the message");
		} else if (e.getSource().equals(d)) {
			System.out.println("Requesting default game");
			crs.askNGAME(ClientRolitSocket.NGAMEFlags.Default);
		} else if (e.getSource().equals(h)) {
			System.out.println("Requesting 2 players");
			crs.askNGAME(ClientRolitSocket.NGAMEFlags.TwoPlayerGame);
		} else if (e.getSource().equals(i)) {
			System.out.println("Requesting 3 players");
			crs.askNGAME(ClientRolitSocket.NGAMEFlags.ThreePlayerGame);
		} else if (e.getSource().equals(j)) {
			System.out.println("Requesting 4 players");
			crs.askNGAME(ClientRolitSocket.NGAMEFlags.FourPlayerGame);
		}
	}
	
	public class SocketHandlerThread extends Thread {
		private ClientRolitSocket crs;
		private MessageType serverMessageType;
		
		public SocketHandlerThread(ClientRolitSocket inputCrs) {
			crs = inputCrs;
		}
		
		@Override
		public void run() {
			String[] newMessage;
			System.out.println("Started the socket handler");
			while (crs.isRunning()) {
				if (crs.isNewMsgQueued()) {
					try {
						//System.out.println("Socket is still running");
						serverMessageType = crs.getQueuedMsgType();
						System.out.println(serverMessageType.toString());
						newMessage = crs.getQueuedMsgArray();
						System.out.println(newMessage);
						// check the type
						switch (serverMessageType) {
							case X_NONE:
								//System.out.println("No action");
								break;
							case AL_CHATM:
								String chatMessage;
								System.out.println("Received chatmessage");
								String[] realMessage = new String[newMessage.length - 1];
								System.arraycopy(newMessage, 1, realMessage, 0, 
											newMessage.length - 1);
								chatMessage = Utils.join(realMessage);
								addChatMessage(newMessage[0], chatMessage);
								break;
							case LO_INVIT:
								System.out.println("Request for starting game");
								System.out.println("Display accept or deny window");
								break;
							case LO_START:
								System.out.println("WOOOW EEN SPEL GAAT STARTEN!, wat spannend!");
								
								break;
							default:
								break;
						}
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}	
	}
	
}
