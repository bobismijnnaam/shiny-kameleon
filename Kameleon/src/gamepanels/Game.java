package gamepanels;

import java.awt.Color;
import java.awt.LayoutManager;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import players.Player;
import board.BoardController;
import board.BoardModel;
import board.BoardView;

public class Game extends JFrame {
	
	public final static int STATE_MAIN = 0;
	public final static int STATE_OFFLINE = 1;
	public final static int STATE_ONLINE = 2;
	
	private JPanel currentState;
	private Player[] players;
	private String[] settings;

	public Game() {
		setSize(600, 600);
		setVisible(true);
		setTitle("Controllit");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
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
				break;
			case STATE_ONLINE:
				currentState = null;
				currentState = new JPanel();
				currentState.setBackground(Color.GREEN);
				add(currentState);
				break;
		}
	}

	public static void main(String[] args) throws IOException {
		Game gm = new Game();
		String[] settings = new String[4];
		gm.setNextState(STATE_MAIN, settings);
		
	}

}
