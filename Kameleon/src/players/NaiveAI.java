package players;

import java.util.LinkedList;

import utility.Vector2i;
import board.BoardModel;

public class NaiveAI extends Player implements AI {
	public NaiveAI(Colour inputColour) {
		super(inputColour, "Naïve");
	}

	@Override
	public Vector2i getMove(BoardModel board) {
		return NaiveAI.getMove(board, this);
	}
	
	public static Vector2i getMove(BoardModel board, Player player) {
		LinkedList<Vector2i> suggestions = board.getMoveSuggestions(player);
		System.out.println("NAIVEAI IS THINKING");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return suggestions.get((int) Math.floor(Math.random() * suggestions.size()));
	}

}
