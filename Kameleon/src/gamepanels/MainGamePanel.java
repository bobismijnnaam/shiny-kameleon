package gamepanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import network.ClientRolitSocket;
import network.SocketHandlerThread;
import players.AlphaAI;
import players.NaiveAI;
import players.NetworkPlayer;
import players.Player;
import players.Player.Colour;
import players.SmartAI;
import utility.BackgroundPanel;
import utility.Move;
import utility.RatioPanel;
import utility.Vector2i;
import board.BoardController;
import board.BoardModel;
import board.BoardView;

public class MainGamePanel extends JInternalFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private String[] settings;
	private Player[] players;
	private BoardModel board;
	private BoardView mainView;
	private BoardController boardController;
	private GridBagConstraints c;
	private Game game;
	private JLayeredPane layeredPane;
	private boolean boardReady = false;
	private ClientRolitSocket crs = null;
	private int currentPlayer = -1;
	private int maxPlayer = 0;
	
	/**
	 * @param inputSettings - a String containing the players (human , disabled, ai)
	 * @throws IOException
	 */
	public MainGamePanel(String[] inputSettings, Game inputGame) throws IOException {
		// attempt to put everything in a layered pane
		layeredPane = new JLayeredPane();
		layeredPane.setLayout(new GridBagLayout());
		game = inputGame;
		settings = inputSettings;
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		board = new BoardModel();
		mainView = new BoardView(board);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		c.gridx = 0;
		c.weightx = 0.5;
		c.weighty = 0.9;
		c.gridy = 0;
		c.gridx = 0;
		BackgroundPanel menuBar = new BackgroundPanel("media/bg.png");
		menuBar.setLayout(new BorderLayout());
		JPanel leftHand = drawLeftHand();
		menuBar.add(leftHand, BorderLayout.CENTER);
		layeredPane.add(menuBar, c);
		c.gridx = 1;
		layeredPane.setLayer(mainView, 0);
		layeredPane.add(mainView.getRootPane(), c);
		add(layeredPane, c);
		setOpaque(false);
	}
	
	public MainGamePanel(String[] inputSettings, Game inputGame, 
			ClientRolitSocket inputCrs) throws IOException {
		System.out.println("CONSTRUCTING ONLINE GAME");
		// attempt to put everything in a layered pane
		layeredPane = new JLayeredPane();
		layeredPane.setLayout(new GridBagLayout());
		game = inputGame;
		settings = inputSettings;
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		board = new BoardModel();
		mainView = new BoardView(board);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		c.gridx = 0;
		c.weightx = 0.5;
		c.weighty = 0.9;
		c.gridy = 0;
		c.gridx = 0;
		BackgroundPanel menuBar = new BackgroundPanel("media/bg.png");
		menuBar.setLayout(new BorderLayout());
		JPanel leftHand = drawLeftHand();
		menuBar.add(leftHand, BorderLayout.CENTER);
		layeredPane.add(menuBar, c);
		c.gridx = 1;
		layeredPane.setLayer(mainView, 0);
		layeredPane.add(mainView.getRootPane(), c);
		add(layeredPane, c);
		setOpaque(false);
		crs = inputCrs;
		SocketHandlerThread onlineHandler = new SocketHandlerThread(inputCrs, game, this);
		onlineHandler.start();
	}
 
	/**
	 * Initializes the players according to the settings and sets start positions.
	 */
	public void setStartPosition() {
		players = new Player[4];
		Colour currentColor = Player.Colour.Red;
		
		maxPlayer = 0;
		for (int i = 0; i < 4; i++) {
			if (settings[i] != null) {
				if (settings[i].equals("human")) {
					players[i] = new Player(currentColor, "Human");
					currentColor = currentColor.getNext();
					maxPlayer++;
				}  else if (settings[i].equals("easy")) {
					players[i] = new NaiveAI(currentColor); // NaiveAI
					currentColor = currentColor.getNext();
					maxPlayer++;
				} else if (settings[i].equals("medium")) {
					players[i] = new AlphaAI(currentColor, "alpha"); // AlphaAI
					currentColor = currentColor.getNext();
					maxPlayer++;
				} else if (settings[i].equals("hard")) {
					players[i] = new SmartAI(currentColor); // SmartAI
					currentColor = currentColor.getNext();
					maxPlayer++;
				} else if (settings[i].equals("network")) {
					players[i] = new NetworkPlayer(currentColor, "network", false);
					currentColor = currentColor.getNext();
					maxPlayer++;
				} else if (settings[i].equals("networkyou")) {
					players[i] = new NetworkPlayer(currentColor, "network", true);
					currentColor = currentColor.getNext();
					maxPlayer++;
				}
			}
		}
		
		if (maxPlayer == 2) {
			board.setStartPosition(players[0], players[1]);
		} else if (maxPlayer == 3) {
			board.setStartPosition(players[0], players[1], players[2]);
		} else if (maxPlayer == 4) {
			board.setStartPosition(players[0], players[1], players[2], players[3]);
		}
		boardReady = true;
	
	}
	
	public boolean getBoardReady() {
		return boardReady;
	}
	
	/**
	 * Adds listeners to the board controller.
	 */
	public void addListeners() {
		if (crs == null) {
			boardController = new BoardController(board, mainView.getFieldButtons(),
					mainView.getFields(), players, this);
			mainView.addListeners(boardController);
		} else {
			boardController = new BoardController(board, mainView.getFieldButtons(),
					mainView.getFields(), players, this, crs);
			mainView.addListeners(boardController);
		}
	}
	
	/**
	 * Sets the player turn to the next player and starts the turn.
	 */
	public void setPlayerTurn() {
		if (board.hasWinner()) {
			System.out.println("We've got a winner");
			showWinner(board.getWinners());
		} else {
		
			currentPlayer++;
			if (currentPlayer == maxPlayer) {
				currentPlayer = 0;
			}
			mainView.setTurn(currentPlayer, players);
			boardController.startPlayerTurn(players[currentPlayer]);
		}
	}
	
	public void showWinner(Player[] inputPlayers) {
		final JDialog dialog = new JDialog();  
		dialog.setModal(true);
	    dialog.setSize(300, 200);
	    dialog.setLocationRelativeTo(mainView);
	    JLabel winner = null;
	    if (inputPlayers.length == 1) {
	    	winner = new JLabel(inputPlayers[0].getColour() + " is the winner with " + 
    				board.getScore(inputPlayers[0]) + "points!", JLabel.CENTER);
	    } else {
	    	for (int i = 0; i < inputPlayers.length; i++) {
	    		String winners = "";
	    		winners = winners + inputPlayers[i].getColour() + " with a score of " + 
	    				board.getScore(inputPlayers[i]) + " | ";
	    		winner = new JLabel(winners);
	    	}
	    }
	    dialog.add(winner);
	    dialog.setVisible(true);
	}
	
	public void setNetworkPlayerTurn(int playerID) {
		mainView.setTurn(playerID, players);
		boardController.startPlayerTurn(players[playerID]);
	}
	
	public JPanel drawLeftHand() {
		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		container.setOpaque(false);
		RatioPanel logoWrapper = new RatioPanel();
		logoWrapper.setLayout(new GridBagLayout());
		GridBagConstraints d = new GridBagConstraints();
		d.fill = GridBagConstraints.BOTH;
		d.weightx = 0.1;
		d.weighty = 0.1;
		d.gridx = 0;
		d.gridy = 0;
		d.gridx = 0;
	
		BackgroundPanel logo = new BackgroundPanel("media/controlit.png");
		logo.setOpaque(false);
		logo.setPreferredSize(new Dimension(500, 200));
		logoWrapper.add(logo, d);
		logoWrapper.setOpaque(false);
		BackgroundPanel mainMenu = new BackgroundPanel("media/mainMenu.png");
		JButton mainMenuButton = new JButton("");
		mainMenuButton.setName("main");
		mainMenuButton.setOpaque(false);
		mainMenuButton.addActionListener(this);
		mainMenu.setLayout(new BorderLayout());
		mainMenuButton.setContentAreaFilled(false);
		mainMenuButton.setBorderPainted(false);
		mainMenu.setPreferredSize(new Dimension(500, 200));
		mainMenu.setOpaque(false);
		mainMenu.add(mainMenuButton, BorderLayout.CENTER);
		d.gridy = 1;
		logoWrapper.add(mainMenu, d);
		d.gridy = 0;
		d.weighty = 0.3;
		container.add(logoWrapper, d);
		d.gridy = 1;
		d.weighty = 0.4;
		JButton test = new JButton("");
		test.setOpaque(false);
		test.setContentAreaFilled(false);
		test.setBorderPainted(false);
		container.add(test, d);
		return container;
	}

	
	public void drawPopup() {
		JButton test = new JButton("test");
		layeredPane.setLayer(test, 10);
		layeredPane.add(test);
		layeredPane.repaint();
	}
	
	public void goToMainMenu() {
		String[] inputSettings = new String[4];
		try {
			game.setNextState(Game.STATE_MAIN, inputSettings, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setOnlineMove(String x, String y) {
		System.out.println("Setting the clients move");
		Vector2i position = new Vector2i(Integer.parseInt(x), Integer.parseInt(y));
		Move theMove = new Move(position, board.getCurrentPlayer());
		if (board.isMoveAllowed(theMove)) {
			board.applyMove(theMove);
			boardController.disableButtons();
			setPlayerTurn();
		} 
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton check = (JButton) e.getSource();
		if (check.getName() == "main") {
			goToMainMenu();
			if (crs != null) {
				System.out.println("Were online close the socket!");
				crs.close();
			}
		}
		
	}
	
}

