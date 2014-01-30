package gamepanels;

import java.io.IOException;

import javax.swing.JFrame;

import network.ClientRolitSocket;

public class Game extends JFrame {
	
	// all the states
	private static final long serialVersionUID = 1L;
	public final static int STATE_MAIN = 0;
	public final static int STATE_OFFLINE = 1;
	public final static int STATE_LOBBY = 2;
	public final static int STATE_ONLINE = 3;
	
	private String[] settings;
	private MainMenu mainMenu;
	private Lobby lobby;
	private MainGamePanel offlineView;
	private MainGamePanel onlineView;
	private int lastState = 0;

	/**
	 * Initializes a new game, sets the size and title of the screen and visible.
	 */
	public Game() {
		setSize(600, 600);
		setTitle("Controllit");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * @param nextState - the next state to go to (constant).
	 * @param inputSettings - inputSettings received out of mainFrame to set the players.
	 * @throws IOException
	 */
	public void setNextState(int nextState, String[] inputSettings,
		ClientRolitSocket inputCrs) throws IOException {
		
		// check which state.
		switch (nextState) {
			case STATE_MAIN:
				// if the lastState was an online or offline game.
				if (lastState == STATE_OFFLINE) {
					remove(offlineView.getRootPane());
				} else if (lastState == STATE_ONLINE) {
					remove(onlineView.getRootPane());
				}
				
				// create a new mainMenu view and add it to the frame.
				mainMenu = new MainMenu(this);
				add(mainMenu);
				// resize to force repaint.
				setSize(650, 620);
				break;
			case STATE_OFFLINE:
				// attempt to create a layered panel
				lastState = STATE_OFFLINE;
				remove(mainMenu);
				setSize(0, 0);
				settings = inputSettings;

				offlineView = new MainGamePanel(settings, this);
				add(offlineView.getRootPane());
				setSize(812, 590);
				
				// forced repaint and start position, add listeners and start turn.
				offlineView.setStartPosition();
				offlineView.addListeners();
				offlineView.setPlayerTurn();
				break;
			case STATE_LOBBY:
				settings = inputSettings;
				remove(mainMenu);
				setSize(0, 0);
				lobby = new Lobby(this, settings);
				add(lobby);
				setSize(720, 620);
				break;
			case STATE_ONLINE:
				remove(lobby);
				lastState = STATE_ONLINE;
				settings = inputSettings;

				setSize(0, 0);
				// drawn a new gamePanel add it to the frame
				onlineView = new MainGamePanel(settings, this, inputCrs);
				add(onlineView.getRootPane());
				setSize(812, 590);
				onlineView.setStartPosition();
				onlineView.addListeners();
				onlineView.setPlayerTurn();
				lastState = STATE_ONLINE;
				break;

		}
	}

	/**
	 * The main function that starts the game by loading the main menu.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final Game gm = new Game();
		String[] settings = new String[4];
		gm.setNextState(STATE_MAIN, settings, null);
	
	}
	
}
