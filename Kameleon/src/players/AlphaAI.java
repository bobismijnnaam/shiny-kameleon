package players;

import java.util.LinkedList;

import utility.Vector2i;
import board.BoardModel;

public class AlphaAI extends Player implements AI {

	public AlphaAI(Colour inputColour, String inputName) {
		super(inputColour, inputName);
		
	}

	@Override
	public Vector2i getMove(BoardModel board) {
		System.out.println("Alpha thinking");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return AlphaAI.getMove(board, this);
	}

	public static Vector2i getMove(BoardModel board, Player player) {
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		Vector2i bestMove = suggestions.get(0);
		String type = "none";
		for (Vector2i i : suggestions) {
			BoardModel nextBoard = board.deepCopy();
			Player nextPlayer = board.getNextPlayer(player);
			LinkedList<Vector2i> enemy = board.getMoveSuggestions(nextPlayer);
			boolean badmove = false;
			for (Vector2i j : enemy) {
				if (i == new Vector2i(0 , 0) || i == new Vector2i(0 , 7) 
					|| i == new Vector2i(7 , 7) || i == new Vector2i(7 , 0)) {
					badmove = true;
				}
			}
			if (!badmove) {
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
