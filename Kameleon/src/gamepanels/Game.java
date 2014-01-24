package gamepanels;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame {
	
	private static final long serialVersionUID = 1L;
	public final static int STATE_MAIN = 0;
	public final static int STATE_OFFLINE = 1;
	public final static int STATE_ONLINE = 2;
	
	private JPanel currentState;
	private String[] settings;

	/**
	 * Initializes a new game, sets the size and title of the screen and visible.
	 */
	public Game() {
		setSize(600, 600);
		setVisible(true);
		setTitle("Controllit");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * @param nextState - the next state to go to (constant).
	 * @param inputSettings - inputSettings received out of mainFrame to set the players.
	 * @throws IOException
	 */
	public void setNextState(int nextState, String[] inputSettings) throws IOException {
		switch (nextState) {
			case STATE_MAIN:
				currentState = null;
				currentState = new MainMenu(this);
				currentState.setBackground(Color.BLACK);
				add(currentState);
				setSize(650, 620);
				break;
			case STATE_OFFLINE:
				System.out.println("Attempt to start an offline game");
				remove(currentState);
				setSize(0, 0);
				System.out.println("Removed the old gamePanel");
				settings = inputSettings;
				System.out.println("Got the settings!");
				
				for (int x = 0; x < 4; x++) {
					System.out.println(settings[x]);
				}
				
				System.out.println("Have drawn the new gamePanel");
				OfflineGame view = new OfflineGame(settings);
				System.out.println("Initilized the board");
				add(view.getRootPane());
				setSize(600, 620);
				view.setStartPosition();
				view.addListeners();
				view.setPlayerTurn();
				break;
			case STATE_ONLINE:
				currentState = null;
				currentState = new JPanel();
				currentState.setBackground(Color.GREEN);
				add(currentState);
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
