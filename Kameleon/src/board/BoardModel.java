package board;

import utility.*;

public class BoardModel {
	
	private Player[][] fields;
	
	public static final int BOARD_W = 8;
	public static final int BOARD_H = 8;
	
	public BoardModel() {
		fields = new Player[BOARD_W][BOARD_H];
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				fields[x][y] = null;
			}
		}
	}
	
	public void applyMove(Move move) {
		Vector2i p = move.getPosition();
		fields[p.x][p.y] = move.getPlayer();
		
		// Evaluate move
	}
	 
	public int getScore(Player player) {
		int score = 0;
		
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				if (fields[x][y] == player) {
					score++;
				}
			}
		}
		
		return score;
	}
	
	public Player getPlayerAt(int x, int y) {
		return fields[x][y];
	}
}
