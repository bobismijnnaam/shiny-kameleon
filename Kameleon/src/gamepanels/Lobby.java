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

import players.Player;
import utility.Utils;
import net.miginfocom.swing.MigLayout;
import network.ClientRolitSocket;
import network.PKISocket;
import network.Server;
import network.RolitSocket.MessageType;
import network.SocketHandlerThread;

public class Lobby extends JPanel implements ActionListener {
	
	// generated unique serialVersionUID
	private static final long serialVersionUID = -3097353574853799434L;
	private String[] settings;
	private JTextArea message;
	private ClientRolitSocket crs;
	private MessageType newMsgType = MessageType.X_NONE;
	private JTextArea chat, playerModus;
	private JTextArea playerName;
	private JButton c, d, h, i, j, send, human, easy, medium, hard;
	private Game game;
	
	/**
	 * Construct a new lobby.
	 * @param inputGame - the Game controller.
	 * @param inputSettings - the settings to build the lobby with, 
	 * settings contain server and user information.
	 */
	public Lobby(Game inputGame, String[] inputSettings) {
		settings = inputSettings;
		game = inputGame;
		createLobby();
	}
	
	/**
	 * Sets a welcome message, tries to set the lobby 
	 * Sockethandler and tries to init and authenticate.
	 */
	public void createLobby() {
		JLabel test = new JLabel("Welcome to the lobby!");
		add(test);
		try {
			authenticate();
			init();
			SocketHandlerThread socketHandler = new SocketHandlerThread(crs, game, this);
			socketHandler.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
			
		}
	}
	
	/**
	 * Tries to start a pki and authenticate.
	 * @throws InterruptedException if thread is interrupted.
	 */
	public void authenticate() throws InterruptedException {
		PKISocket pki = new PKISocket(settings[0], settings[1]);
		pki.start();
		
		// wait for key to get ready
		while (!pki.isPrivateKeyReady()) {
			Thread.sleep(100);
		}
		
		// start the lobby socket
		crs = new ClientRolitSocket(settings[2], Integer.parseInt(settings[3]));
		crs.start();
		
		while (!crs.isRunning()) {
			Thread.sleep(100);
		}
	
		// ask for login
		crs.askLOGIN(settings[0]);
		
		while (newMsgType != MessageType.AC_VSIGN) {
			newMsgType = crs.getQueuedMsgType();
			Thread.sleep(100);
		}
		
		// sign the string
		String toSign = crs.getQueuedMsg();
		crs.tellVSIGN(toSign, pki.getPrivateKey());
	}
	
	/**
	 * Function to be called after authenticating. 
	 * Waits for hello message and draws the lobby elements according to the flags,
	 * then returns hello back.
	 */
	public void init() {
		System.out.println("Waiting for bob to implement hello..");
		while (newMsgType != MessageType.AC_HELLO) {
			newMsgType = crs.getQueuedMsgType();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	
	/**
	 * Adds default buttons to the screen.
	 */
	public void drawDefault() {
		JLabel title = new JLabel("Welcome to the default online game " + settings[0]);
		add(title);
		// the buttons which can be used to ask NGAME
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
		// player modus buttons
		playerModus = new JTextArea("human");
		human = new JButton("Play online as a human player");
		human.setName("human");
		human.addActionListener(this);
		easy = new JButton("Play online as a easy computer");
		easy.setName("medium");
		easy.addActionListener(this);
		medium = new JButton("Play online as a medium computer");
		medium.setName("hard");
		medium.addActionListener(this);
		hard = new JButton("Play online as a hard computer");
		hard.setName("hard");
		hard.addActionListener(this);
		add(playerModus);
		add(human);
		add(easy);
		add(medium);
		add(hard);
		add(d);
		add(h);
		add(i);
		add(j);
	}
	
	/**
	 * Draws information to the screen about what features the lobby has.
	 */
	public void drawLobby() {
		JLabel title = new JLabel("Welcome to the lobby " + settings[0]);
		add(title);
	}
	
	/**
	 * Draws a chatbox and adds listeners.
	 */
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
	
	/** 
	 * Adds a message to the chatBox.
	 * @param inputUsername - The user that sends the message.
	 * @param inputMessage - The message.
	 */
	public void addChatMessage(String inputUsername, String inputMessage) {
		chat.append("\n" + inputUsername + ": " + inputMessage);
	}

	/** 
	 * listener that waits for an action.
	 */
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
		} else if (e.getSource().equals(human)) {
			playerModus.setText(human.getName());
		} else if (e.getSource().equals(easy)) {
			playerModus.setText(easy.getName());
		} else if (e.getSource().equals(medium)) {
			playerModus.setText(medium.getName());
		} else if (e.getSource().equals(hard)) {
			playerModus.setText(hard.getName());
		}
	}
	
	/** 
	 * @return An String array containing the lobby settings (username, password etc).
	 */
	public String[] getSettings() {
		return settings;
	}
	
	/**
	 * @return Returns if an AI of human is playing.
	 */
	public String getPlayerModus() {
		System.out.println(playerModus.getText());
		return playerModus.getText();
	}
	
}
