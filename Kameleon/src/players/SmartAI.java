package players;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import utility.Vector2i;
import board.BoardModel;

public class SmartAI extends Player implements AI {
	
	public static int[][] lt = new int[][]{
		{7, 2, 5, 4, 4, 5, 2, 7},
		{2, 1, 3, 3, 3, 3, 1, 2},
		{5, 3, 6, 5, 5, 6, 3, 5},
		{4, 3, 5, 6, 6, 5, 3, 4},
		{4, 3, 5, 6, 6, 5, 3, 4},
		{5, 3, 6, 5, 5, 6, 3, 5},
		{2, 1, 3, 3, 3, 3, 1, 2},
		{7, 2, 5, 4, 4, 5, 2, 7},
	};

	public SmartAI(Colour inputColour) {
		super(inputColour, "Smart");
	}

	@Override
	public Vector2i getMove(BoardModel board) {
		return SmartAI.getMove(board, this);
	}
	
	public static Vector2i getMove(BoardModel board, Player player) {
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		Vector2i bestMove = suggestions.getFirst();
		int bestGrade = 0;
		
		for (Vector2i pos : suggestions) {
			int grade = lt[pos.x][pos.y];
			if (grade > bestGrade) {
				bestMove = new Vector2i(pos);
			}
		}
		System.out.println(bestMove == null);
		return bestMove;
	}

}
