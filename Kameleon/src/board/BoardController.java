package board;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;

import players.Player;
import utility.BackgroundPanel;
import utility.Move;
import utility.Vector2i;

public class BoardController implements ActionListener {
	
	private BoardModel board;
	private JButton[][] fieldButtons;
	private BackgroundPanel[][] fields;
	private Player[] players;
	private Player currentPlayer;
	
	public BoardController(BoardModel inputBoard, JButton[][] inputFieldButtons, 
			BackgroundPanel[][] inputFields, Player[] inputPlayers) {
		
		board = inputBoard;
		fieldButtons = inputFieldButtons;
		fields = inputFields;
		players = inputPlayers;
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
		enableButtons(currentPlayer);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// If this gets triggered buttons are enabled and thus it is a human players' turn!
		JButton change = (JButton) e.getSource();
		String name = change.getName();
		String[] split = name.split("-");
		
		Vector2i position = new Vector2i(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		Move theMove = new Move(position, currentPlayer);
		if (board.isMoveAllowed(theMove)) {
			board.applyMove(theMove);
			System.out.println(board.toString());
			disableButtons();
			// Signal to outer game class that the turn has been done?
		} else {
			// Give feedback to user? Status screen update?
		}
	}

}
