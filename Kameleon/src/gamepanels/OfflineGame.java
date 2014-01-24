package gamepanels;

import java.io.IOException;

import javax.swing.JInternalFrame;

import players.NaiveAI;
import players.Player;
import players.Player.Colour;
import board.BoardController;
import board.BoardModel;
import board.BoardView;

public class OfflineGame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	String[] settings;
	Player[] players;
	BoardModel board;
	BoardView mainView;
	BoardController boardController;
	
	int currentPlayer = -1;
	int maxPlayer = 0;
	
	/**
	 * @param inputSettings - a String containing the players (human , disabled, ai)
	 * @throws IOException
	 */
	public OfflineGame(String[] inputSettings) throws IOException {
		settings = inputSettings;
		
		System.out.println("Created a new board");
		board = new BoardModel();
		System.out.println("Created a new board view");
		mainView = new BoardView(board);
		add(mainView.getRootPane());
	}
 
	/**
	 * Initializes the players according to the settings and sets start positions.
	 */
	public void setStartPosition() {
		players = new Player[4];
		Colour currentColor = Player.Colour.Red;
		
		maxPlayer = 0;
		for (int i = 0; i < 4; i++) {
			System.out.println("Settings is:" + settings[i] + "|");
			if (settings[i].equals("human")) {
				players[i] = new Player(currentColor, "Human");
				System.out.println("Set a human player");
				currentColor = currentColor.getNext();
				maxPlayer++;
			}  else if (settings[i].equals("easy")) {
				players[i] = new NaiveAI(currentColor);
				System.out.println("Set a easy computer");
				currentColor = currentColor.getNext();
				maxPlayer++;
			}
		}
		
		if (maxPlayer == 2) {
			board.setStartPosition(players[0], players[1]);
		} else if (maxPlayer == 3) {
			board.setStartPosition(players[0], players[1], players[2]);
		} else if (maxPlayer == 4) {
			board.setStartPosition(players[0], players[1], players[2], players[3]);
		}
	}
	
	/**
	 * Adds listeners to the board controller.
	 */
	public void addListeners() {
		boardController = new BoardController(board, mainView.getFieldButtons(),
				mainView.getFields(), players, this);
		mainView.addListeners(boardController);
	}
	
	/**
	 * Sets the player turn to the next player and starts the turn.
	 */
	public void setPlayerTurn() {
		currentPlayer++;
		if (currentPlayer == maxPlayer) {
			currentPlayer = 0;
		}
		boardController.startPlayerTurn(players[currentPlayer]);
	}
	
}
