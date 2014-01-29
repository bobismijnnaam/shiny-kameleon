package players;

import java.util.LinkedList;

import utility.Vector2i;
import board.BoardModel;

public class AlphaAI extends Player implements AI {

	/**
	 * Constructor for AlphaAI, alpha AI is a constraint based AI.
	 * @param inputColour - The AI colour.
	 * @param inputName - The name of the Ai.
	 */
	public AlphaAI(Colour inputColour, String inputName) {
		super(inputColour, inputName);
		
	}

	/**
	 * @return The best move found by the AlphaAI.
	 */
	@Override
	public Vector2i getMove(BoardModel board) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("AlphaAI was interrupted");
		}
		return AlphaAI.getMove(board, this);
	}

	/**
	 * Get's the best move according to constraints.
	 * @param board - the current board we're playing on.
	 * @param player - The player to calculate the best move for.
	 * @return The best move according to the AlphaAI
	 */
	public static Vector2i getMove(BoardModel board, Player player) {
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		// initiliaze the first move
		Vector2i bestMove = suggestions.get(0);
		// set the type to none;
		String type = "none";
		// loop trough all suggestions
		for (Vector2i i : suggestions) {
			BoardModel nextBoard = board.deepCopy();
			Player nextPlayer = board.getNextPlayer(player);
			// check if the move is done if the enemy can take a corner
			LinkedList<Vector2i> enemy = board.getMoveSuggestions(nextPlayer);
			boolean badmove = false;
			for (Vector2i j : enemy) {
				if (i == new Vector2i(0 , 0) || i == new Vector2i(0 , 7) 
					|| i == new Vector2i(7 , 7) || i == new Vector2i(7 , 0)) {
					badmove = true;
				}
			}
			// when it can't take a corner
			if (!badmove) {
				// try to pick a corner
				if (i == new Vector2i(0, 0)) {
					bestMove = i;
					type = "corner";
				} else if (i == new Vector2i(7, 7)) {
					bestMove = i;
					type = "corner";
				} else if (i == new Vector2i(0, 7)) {
					bestMove = i;
					type = "corner";
				} else if (i == new Vector2i(7, 0)) {
					bestMove = i;
					type = "corner";
				// try to pick a place located good for a corner
				} else if (i == new Vector2i(0, 2)) {
					if (!type.equals("corner")) {
						bestMove = i;
						type = "close";
					}
				} else if (i == new Vector2i(0, 5)) {
					if (!type.equals("corner")) {
						bestMove = i;
						type = "close";
					}
				} else if (i == new Vector2i(7, 2)) {
					if (!type.equals("corner")) {
						bestMove = i;
						type = "close";
					}
				} else if (i == new Vector2i(7, 5)) {
					if (!type.equals("corner")) {
						bestMove = i;
						type = "close";
					}
				} else if (i == new Vector2i(3, 7)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(4, 7)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(4, 0)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(3, 0)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(0, 3)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(0, 4)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(3, 7)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				} else if (i == new Vector2i(4, 7)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
						type = "semiclose";
					}
				// try to avoid bad moves
				} else if (i != new Vector2i(0, 1) || i != new Vector2i(1 , 0)
					|| i != new Vector2i(0, 6) || i != new Vector2i(6, 0) ||
						i != new Vector2i(7, 6) || i != new Vector2i(6, 7)) {
					if (!type.equals("corner") && !type.equals("close")) {
						bestMove = i;
					}
				}
			}
		}
		return bestMove;
	}
}
