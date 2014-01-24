package board;

import gamepanels.OfflineGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;

import players.AI;
import players.Player;
import utility.BackgroundPanel;
import utility.Move;
import utility.Vector2i;

public class BoardController implements ActionListener {
	
	private BoardModel board;
	private JButton[][] fieldButtons;
	//private BackgroundPanel[][] fields;
	//private Player[] players;
	private Player currentPlayer;
	private OfflineGame offlineGame;
	
	public BoardController(BoardModel inputBoard, JButton[][] inputFieldButtons, 
			BackgroundPanel[][] inputFields, Player[] inputPlayers, OfflineGame inputGame) {
		
		board = inputBoard;
		fieldButtons = inputFieldButtons;
		//fields = inputFields;
		//players = inputPlayers;
		offlineGame = inputGame;
		disableButtons();
	}
	
	public void enableButtons(Player player) {
		LinkedList<Vector2i> validMoves = board.getMoveSuggestions(player);
		for (Vector2i move : validMoves) {
			fieldButtons[move.x][move.y].setEnabled(true); // MAGIC HAPPENS HERE
		}
	}
	
	public void disableButtons() {
		for (int x = 0; x < BoardModel.BOARD_W; x++) {
			for (int y = 0; y < BoardModel.BOARD_H; y++) {
				fieldButtons[x][y].setEnabled(false); // YOUR MISCHIEVOUS SCHEME ENDS HERE, SIR
			} // and ur turn also l0l
		}
	}
	
	public void startPlayerTurn(Player player) {
		currentPlayer = player; // Humanplayer/networkplayer/computerplayer
		board.setCurrentPlayer(currentPlayer);
		System.out.println("Started player turn");
		System.out.println("Set the player!!");
		//Vector2i position = new Vector2i(0, 0);
		//Move theMove;
		if (player instanceof AI) {
			AIThread ai = new AIThread(player);
			ai.start();
			/*System.out.println("HOLY SJIT IT's a COMPUTERS TURN!");
			enableButtons(currentPlayer);
			position = ((AI) player).getMove(board);
			fieldButtons[position.x][position.y].doClick();
			System.out.println("PUSS THAT BUTTON!!!");*/
		} else {
			enableButtons(currentPlayer);
		}
	}
	
	// AI thread
	public class AIThread extends Thread {
		
		private Player player;
		
		public AIThread(Player inputPlayer) {
			player = inputPlayer;
		}
		
		@Override
		public void run() {
			System.out.println("HOLY SJIT IT's a COMPUTERS TURN!");
			enableButtons(currentPlayer);
			Vector2i position = ((AI) player).getMove(board);
			fieldButtons[position.x][position.y].doClick();
			System.out.println("PUSS THAT BUTTON!!!");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		System.out.println("Board changed");
		// If this gets triggered buttons are enabled and thus it is a human players' turn!
		JButton change = (JButton) e.getSource();
		String name = change.getName();
		String[] split = name.split("-");
		
		Vector2i position = new Vector2i(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		Move theMove = new Move(position, board.getCurrentPlayer());
		System.out.println("Got the player!!!!");
		if (board.isMoveAllowed(theMove)) {
			board.applyMove(theMove);
			System.out.println(board.toString());
			disableButtons();
			// Signal to outer game class that the turn has been done?
		} 
		offlineGame.setPlayerTurn();
	}

}
