package players;

import java.util.ArrayList;
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
	
	public static final int MAX_LVL = 5;

	public SmartAI(Colour inputColour) {
		super(inputColour, "Smart");
	}

	@Override
	public Vector2i getMove(BoardModel board) {
		return SmartAI.getMove(board, this);
	}
	
	public static Vector2i getMove(BoardModel board, Player player) {
		ArrayList<Vector2i> suggestions = new ArrayList(board.getMoveSuggestions(player));
		int bestGrade = 9999999;
		Vector2i bestMove = suggestions.get(0);
		
		for (int i = 0; i < suggestions.size(); i++) {
			BoardModel nextBoard = board.deepCopy();
			Move move = new Move(suggestions.get(i), player);
			nextBoard.applyMove(move);
			int grade = eval(nextBoard, board.getNextPlayer(player), 0);
			if (grade < bestGrade) {
				bestGrade = grade;
				bestMove = suggestions.get(i);
			}
		}
		
		return bestMove;
	}
	
	// TODO: Minimizes next players score
	// TODO: chooses to maximize own score (minimize all other scores)
	public static int eval(BoardModel board, Player player, int lvl) {
		if (lvl == MAX_LVL) { // Max Depth is reached
			return board.getGrade(player);
		} else { // Go deeper another layer
			ArrayList<Vector2i> suggestions = new ArrayList<Vector2i>(
					board.getMoveSuggestions(player));
			// int[] grades = new int[suggestions.size()];
			int bestGrade = 999999999;
			for (int i = 0; i < suggestions.size(); i++) {
				BoardModel nextBoard = board.deepCopy();
//				Player nextPlayer = board.getNextPlayer(player);
				Move move = new Move(suggestions.get(i), player);
				nextBoard.applyMove(move);
//				grades[i] = eval(nextBoard, nextPlayer, lvl + 1);
				int grade = eval(nextBoard, board.getNextPlayer(player), lvl + 1);
				if (grade < bestGrade) {
					bestGrade = grade;
				}
			}
			
			return bestGrade;
		}
	}
	
	// OLD BAD AI
	public static Vector2i oldAI(BoardModel board, Player player) {
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		Vector2i bestMove = new Vector2i(0, 0);
		Vector2i bestGrade = new Vector2i(999, 999);
		
		for (Vector2i pos : suggestions) {
			BoardModel nextBoard = board.deepCopy();
			Player nextPlayer = board.getNextPlayer(player);
			nextBoard.applyMove(new Move(pos, nextPlayer));
			Vector2i grade = oldEvalMove(nextBoard, nextPlayer);
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
	
	public static Vector2i oldEvalMove(BoardModel board, Player player) {
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
