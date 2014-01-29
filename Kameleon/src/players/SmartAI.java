package players;

import java.util.LinkedList;

import utility.Move;
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
	
	public class GradedMoveList {
		
	}

	public SmartAI(Colour inputColour) {
		super(inputColour, "Smart");
	}

	@Override
	public Vector2i getMove(BoardModel board) {
		return SmartAI.getMove(board, this);
	}
	
	public static Vector2i getMove(BoardModel board, Player player) {
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		Vector2i bestMove = new Vector2i(0, 0);
		Vector2i bestGrade = new Vector2i(999, 999);
		
		for (Vector2i pos : suggestions) {
			BoardModel nextBoard = board.deepCopy();
			Player nextPlayer = board.getNextPlayer(player);
			nextBoard.applyMove(new Move(pos, nextPlayer));
			Vector2i grade = evalMove(nextBoard, nextPlayer);
			if (grade.x < bestGrade.x) {
				bestGrade = grade;
				bestMove = pos;
			} else if (grade.x == bestGrade.x) {
				if (grade.y < bestGrade.y) {
					bestGrade = grade;
					bestMove = pos;
				}
			}
		}
		
		return bestMove;
	}
	
	public static Vector2i evalMove(BoardModel board, Player player) {
		Vector2i grading = new Vector2i(0, 0);
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		for (Vector2i p : suggestions) {
			int grade = lt[p.x][p.y];
			if (grade > grading.x) {
				grading.x = grade;
				grading.y = 1;
			} else if (grade == grading.x) {
				grading.y++;
			}
		}
		
		return grading;
	}

}
