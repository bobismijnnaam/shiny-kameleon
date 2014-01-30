package gamepanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;
import network.ClientRolitSocket;
import network.INVITStatus;
import network.PKISocket;
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
	private JButton d, h, i, j, send, human, easy, medium, hard;
	private Game game;
	private int lMaxMessage = 0;
	private JPanel list, challenge;
	private ArrayList<JButton> listPlayer = new ArrayList<JButton>();
	
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
		setLayout(new MigLayout());
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
		while (newMsgType != MessageType.AC_HELLO) {
			newMsgType = crs.getQueuedMsgType();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		String hello = crs.getQueuedMsg();
		
		// say hello back to server
		crs.tellHELLO();
		
		hello = hello.trim();
		if (hello.equals("D")) {
			drawDefault();
		} else if (hello.equals("DC")) {
			drawDefault();
			drawChat();
		} else if (hello.equals("CL")) {
			drawDefault();
			drawChat();
			drawChallenge();
		}
	}
	
	/**
	 * Adds default buttons to the screen.
	 */
	public void drawDefault() {
		JLabel title = new JLabel("Welcome to the online game lobby " + settings[0] +
				" You are playing as: ");
		add(title, "span, split 2, width 60%, height 10%");
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
		playerModus.setEditable(false);
		human = new JButton("Human");
		human.setName("human");
		human.addActionListener(this);
		easy = new JButton("EasyAI");
		easy.setName("easy");
		easy.addActionListener(this);
		medium = new JButton("MediumAI");
		medium.setName("medium");
		medium.addActionListener(this);
		hard = new JButton("HardAI");
		hard.setName("hard");
		hard.addActionListener(this);
		add(playerModus, "width 40%");
		add(human, "span, split 4, width 25%, height 10%");
		add(easy, "width 25%, height 10%");
		add(medium, "width 25%, height 10%");
		add(hard, "width 25%, height 10%");
		add(d, "span, split 4, width 25%, height 10%");
		add(h, "width 25%, height 10%");
		add(i, "width 25%, height 10%");
		add(j, "width 25%, height 10%");
	}
	
	/**
	 * Draws information to the screen about what features the lobby has.
	 */
	public void drawLobby() {
		JLabel title = new JLabel("Welcome to the lobby " + settings[0]);
		add(title);
	}
	
	public void addPlayer(String inputPlayer) {
		if (!inputPlayer.equals(settings[0])) {
			System.out.println(inputPlayer);
			JButton newPlayer = new JButton(inputPlayer);
			newPlayer.setName(inputPlayer);
			list.add(newPlayer, "span, width 100%");
			newPlayer.addActionListener(this);
			game.setSize(721, 620);
			game.setSize(720, 620);
			listPlayer.add(newPlayer);
		}
	}
	
	public void removePlayer(String inputPlayer) {
		System.out.println(inputPlayer + " Has left the game");
		for (JButton leaver : listPlayer) {
			if (leaver.getName().equals(inputPlayer)) {
				System.out.println(leaver.getName());
				list.remove(leaver);
				game.setSize(721, 620);
				game.setSize(720, 620);
			}
		}
	}
	
	/**
	 * Draws a chatbox and adds listeners.
	 */
	public void drawChat() {
		JPanel chatBox = new JPanel();
		chatBox.setLayout(new MigLayout());
		chat = new JTextArea("hii, i'm bob welcome to our fabulous chatbox!");
		chat.setBorder(BorderFactory.createLineBorder(Color.black));
		chat.setEditable(false);  
		chatBox.add(chat, "span, width 100%, height 90%");
		message = new JTextArea("Message:");
		message.setBorder(BorderFactory.createLineBorder(Color.black));
		send = new JButton("Send");
		send.setName("send");
		chatBox.add(message, "span, split 2, width 60%, height 10%");
		chatBox.add(send, "width 40%, height 10%");
		send.addActionListener(this);
		add(chatBox, "span, split 2, width 60%, height 70%");
	}
	
	public void drawChallenge() {
		challenge = new JPanel();
		challenge.setLayout(new MigLayout());
		list = new JPanel(new MigLayout());
		list.setBorder(BorderFactory.createLineBorder(Color.black));
		list.add(new JLabel("Click on an online player below to challenge"), "span, width 100%");
		challenge.add(list, "span, width 100%, height 100%");
		add(challenge, "width 40%, height 70%");
	}
	
	/** 
	 * Adds a message to the chatBox.
	 * @param inputUsername - The user that sends the message.
	 * @param inputMessage - The message.
	 */
	public void addChatMessage(String inputUsername, String inputMessage) {
		chat.append("\n" + inputUsername + ": " + inputMessage);
		lMaxMessage++;
		if (lMaxMessage > 20) {
			System.out.println("Chat full removing upper line");
			int end = 0;
			try {
				end = chat.getLineEndOffset(0);
			} catch (BadLocationException e) {
				e.printStackTrace();
			} 
			chat.replaceRange("", 0, end);
		}
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
		} else {
			// it's an invite
			String toInvite = ((Component) e.getSource()).getName();
			System.out.println("inviting:" + toInvite);
			invite(((Component) e.getSource()).getName());
		}
	}
	
	/** 
	 * @return An String array containing the lobby settings (username, password etc).
	 */
	public String[] getSettings() {
		return settings;
	}
	
	public void invite(String inputName) {
		final JDialog dialog = new JDialog();  
		dialog.setModal(true);
	    dialog.setSize(300, 200);
	    dialog.setLocationRelativeTo(this);
	    JLabel inviteMessage = new JLabel("Invited " + inputName,  JLabel.CENTER);
	    crs.askINVIT(inputName);
	    dialog.add(inviteMessage);
	    dialog.setVisible(true);
	}
	
	public void invitDenied() {
		final JDialog dialog = new JDialog();  
		dialog.setModal(true);
	    dialog.setSize(300, 200);
	    dialog.setLocationRelativeTo(this);
	    JLabel inviteMessage = new JLabel("Invitation denied",  JLabel.CENTER);
	    dialog.add(inviteMessage);
	    dialog.setVisible(true);
	}
	
	
	public void answerInvite(String playerName) {
		int choice = JOptionPane.showConfirmDialog(this 
                , "You are challenged by " + playerName
                , "Game challange" , JOptionPane.WARNING_MESSAGE
                , JOptionPane.OK_CANCEL_OPTION);
		System.out.println(choice);
		if (choice == 0) {
			crs.tellINVIT(INVITStatus.Accept);
		} else {
			crs.tellINVIT(INVITStatus.Denied);
		}
	}
	
	/**
	 * @return Returns if an AI of human is playing.
	 */
	public String getPlayerModus() {
		System.out.println(playerModus.getText());
		return playerModus.getText();
	}
	
}
