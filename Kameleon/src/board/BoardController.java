package board;

import gamepanels.MainGamePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;

import network.ClientRolitSocket;
import players.AI;
import players.NetworkPlayer;
import players.Player;
import utility.BackgroundPanel;
import utility.Move;
import utility.Vector2i;

public class BoardController implements ActionListener {
	
	private BoardModel board;
	private JButton[][] fieldButtons;
	private Player currentPlayer;
	private MainGamePanel mainGamePanel;
	private ClientRolitSocket crs = null;
	private boolean online = false;
	
	/**
	 * BoardController used in a offlineGame.
	 * @param inputBoard - the board.
	 * @param inputFieldButtons - the buttons on the board.
	 * @param inputFields - texture of a field.
	 * @param inputPlayers - the players linked to the board.
	 * @param inputMainGamePanel - the class that draws the panel.
	 */
	public BoardController(BoardModel inputBoard, JButton[][] inputFieldButtons, 
			BackgroundPanel[][] inputFields, Player[] inputPlayers,
			MainGamePanel inputMainGamePanel) {
		
		board = inputBoard;
		fieldButtons = inputFieldButtons;
		mainGamePanel = inputMainGamePanel;
		
		// for AI functionality
		inputBoard.setPlayers(inputPlayers);
		disableButtons();
		online = false;
	}
	
	/**
	 * BoardController for onlineGame.
	 * @param inputBoard - the board.
	 * @param inputFieldButtons - the buttons on the board.
	 * @param inputFields - texture of a field.
	 * @param inputPlayers - the players linked to the board.
	 * @param inputMainGamePanel - the class that draws the panel.
	 * @param inputCrs - ClientRolitSocket the current socket
	 */
	public BoardController(BoardModel inputBoard, JButton[][] inputFieldButtons, 
			BackgroundPanel[][] inputFields, Player[] inputPlayers,
			MainGamePanel inputMainGamePanel, ClientRolitSocket inputCrs) {
		crs = inputCrs;
		board = inputBoard;
		fieldButtons = inputFieldButtons;
		mainGamePanel = inputMainGamePanel;
		disableButtons();
		
		// For AI functionality
		inputBoard.setPlayers(inputPlayers);
		online = true;
	}
	
	/**
	 * Enables the buttons that the player is allowed to click on.
	 * @param player the current player.
	 */
	public void enableButtons(Player player) {
		LinkedList<Vector2i> validMoves = board.getMoveSuggestions(player);
		for (Vector2i move : validMoves) {
			fieldButtons[move.x][move.y].setEnabled(true); 
		}
	}
	
	/**
	 * Disables all the buttons on the field.
	 */
	public void disableButtons() {
		for (int x = 0; x < BoardModel.BOARD_W; x++) {
			for (int y = 0; y < BoardModel.BOARD_H; y++) {
				fieldButtons[x][y].setEnabled(false); 
			} 
		}
	}
	
	/**
	 * Check if the state of the controller is online or offline.
	 * @return false if offline true if onine.
	 */
	public boolean isOnline() {
		return online;
	}
	
	/**
	 * Starts the player turn by setting the current player and waiting for click event.
	 * @param player the player to set the turn for.
	 */
	public void startPlayerTurn(Player player) {
		// can be a human network or AI.
		currentPlayer = player; 
		board.setCurrentPlayer(currentPlayer);
		
		// if the player is an AI
		if (player instanceof AI) {
			// starts a new thread to wait for the AI (allows the panel to redraw properly)
			AIThread ai = new AIThread(player);
			ai.start();
		} else {
			// if the player is an network player
			if (player instanceof NetworkPlayer) {
				if (((NetworkPlayer) player).checkYou()) {
					enableButtons(currentPlayer);
				} 
			} else {
				enableButtons(currentPlayer);
			}
		}
	}
	
	// the thread class for the AI
	public class AIThread extends Thread {
		
		private Player player;
		
		public AIThread(Player inputPlayer) {
			player = inputPlayer;
		}
		
		@Override
		public void run() {
			enableButtons(currentPlayer);
			Vector2i position = ((AI) player).getMove(board);
			fieldButtons[position.x][position.y].doClick();
		}
	}

	/**
	 * If an button is clicked (or simulated by an AI or network player).
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// get the x and y 
		JButton change = (JButton) e.getSource();
		String name = change.getName();
		String[] split = name.split("-");
		
		Vector2i position = new Vector2i(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		Move theMove = new Move(position, board.getCurrentPlayer());
		if (board.isMoveAllowed(theMove)) {
			if (isOnline()) {
				if (board.getCurrentPlayer() instanceof NetworkPlayer 
						|| board.getCurrentPlayer() instanceof AI) {
					crs.tellGMOVE(position.x, position.y);
				}
			}
			board.applyMove(theMove);
			disableButtons();
			mainGamePanel.setPlayerTurn();
		}

	}
}
