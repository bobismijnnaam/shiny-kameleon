package gamepanels;

import java.awt.Color;
import java.io.IOException;
import java.lang.Thread.State;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class Game extends JFrame {
	
	private static final long serialVersionUID = 1L;
	public final static int STATE_MAIN = 0;
	public final static int STATE_OFFLINE = 1;
	public final static int STATE_LOBBY = 2;
	public final static int STATE_ONLINE = 3;
	
	private JPanel currentState;
	private String[] settings;
	private MainMenu mainMenu;
	private Lobby lobby;
	private MainGamePanel view;
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
	public void setNextState(int nextState, String[] inputSettings) throws IOException {
		switch (nextState) {
			case STATE_MAIN:
				if (lastState == STATE_OFFLINE) {
					System.out.println("state was offline");
					remove(view.getRootPane());
				}
				mainMenu = new MainMenu(this);
				add(mainMenu);
				setSize(650, 620);
				break;
			case STATE_OFFLINE:
				// attempt to create a layered panel
				lastState = STATE_OFFLINE;
				System.out.println("Attempt to start an offline game");
				remove(mainMenu);
				setSize(0, 0);
				System.out.println("Removed the old gamePanel");
				settings = inputSettings;
				System.out.println("Got the settings!");
				
				for (int x = 0; x < 4; x++) {
					System.out.println(settings[x]);
				}
				System.out.println("Have drawn the new gamePanel");
				view = new MainGamePanel(settings, this);
				System.out.println("Initilized the board");
				add(view.getRootPane());
				setSize(812, 590);
				view.setStartPosition();
				view.addListeners();
				view.setPlayerTurn();
				break;
			case STATE_LOBBY:
				settings = inputSettings;
				remove(mainMenu);
				setSize(0, 0);
				lobby = new Lobby(this, settings);
				add(lobby);
				setSize(620, 620);
				break;
			case STATE_ONLINE:
				remove(lobby);
				settings = inputSettings;
				for (int i = 0; i < settings.length; i++) {
					System.out.println(settings[i]);
				}
				setSize(0, 0);
				System.out.println("online game is ready");
				System.out.println("Have drawn the new gamePanel");
				view = new MainGamePanel(settings, this);
				System.out.println("Initilized the board");
				add(view.getRootPane());
				setSize(812, 590);
				view.setStartPosition();
				view.addListeners();
				view.setPlayerTurn();
				setSize(812, 590);
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
		gm.setNextState(STATE_MAIN, settings);
	
	}
	
}

//Scanner scr = new Scanner("HI LO\n");
//System.out.println(scr.next());
//System.out.println(scr.next());
//System.out.println(scr.hasNextLine());
//System.out.println(scr.nextLine());
//System.out.println(scr.hasNextLine());
