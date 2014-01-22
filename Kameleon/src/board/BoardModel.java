package board;

import java.util.LinkedList;
import java.util.Observable;

import utility.*;

// TODO
// getField methode
// PlayerColour.next (voor beurten en shit)

public class BoardModel extends Observable {
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
	
	public void setStartPosition(Player p1, Player p2, Player p3, Player p4) {
		fields[3][3] = p1;
		fields[4][3] = p2;
		fields[3][4] = p4;
		fields[4][4] = p3;
		
        setChanged();
        notifyObservers();
	}
	
	public void applyMove(Move move) {
		Vector2i p = move.getPosition();
		fields[p.x][p.y] = move.getPlayer();
		
		Vector2i inbetweenPos;
		Vector2i nextPos;
		
		// Iterate over directions
		for (int i = Vector2i.Direction.MIN_INT; i < Vector2i.Direction.MAX_INT; i++) {
			nextPos = getNextPosition(move.getPosition(), move.getPlayer(), i);
			System.out.println(nextPos);
			
			if (nextPos != null && containsPosition(nextPos)) {
				// An endpoint was found! Iterate over all points inbetween p & nextPos
				inbetweenPos = new Vector2i(p);
				inbetweenPos = inbetweenPos.getNeighbour(i);
				
				while (!nextPos.equals(inbetweenPos)) {
					fields[inbetweenPos.x][inbetweenPos.y] = move.getPlayer();
					inbetweenPos = inbetweenPos.getNeighbour(i);
				}
				
				fields[inbetweenPos.x][inbetweenPos.y] = move.getPlayer();
			}
		}
		
        setChanged();
        notifyObservers();
	}
	
	public Vector2i getNextPosition(Vector2i p, Player player, Vector2i.Direction dir) {
		return getNextPosition(p, player, dir.toInt());
	}
	
	public Vector2i getNextPosition(Vector2i p, Player player, int dir) {
		Vector2i nextPos = p.getNeighbour(dir);

		while (containsPosition(nextPos) 
				&& getPlayerAt(nextPos) != null && getPlayerAt(nextPos) != player) {
			nextPos = nextPos.getNeighbour(dir);
		}
			
		if (!containsPosition(nextPos)) {
			return null;
		} else if (getPlayerAt(nextPos) == null) {
			return null;
		} else {
			return nextPos;
		}
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
	
	public Player getPlayerAt(Vector2i p) {
		//System.out.println("getPlayer: " + p.toString());
		return fields[p.x][p.y];
	}
	
	public Player getNeighbour(Vector2i p, Vector2i.Direction dir) {
		return getNeighbour(p, dir.toInt());
	}
	
	public Player getNeighbour(Vector2i p, int dir) {
		Vector2i newPos = p.getNeighbour(dir);
		return fields[newPos.x][newPos.y];
	}
	
	public boolean containsPosition(Vector2i p) {
		if (p == null) {
			return false;
		} else if (p.x < BOARD_W && p.y < BOARD_H && p.x >= 0 && p.y >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isNeighbourOfBall(Vector2i p) {
		for (int i = Vector2i.Direction.MIN_INT; i < Vector2i.Direction.MAX_INT; i++) {
			if (getNeighbour(p, i) != null) {
				return true;
			}
		}
		
		return false;
	}
	
	public LinkedList<Vector2i> getPossibleMoves() {
		LinkedList<Vector2i> positions = new LinkedList<Vector2i>();
		Vector2i tempVec;
		
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				tempVec = new Vector2i(x, y);
				if (isNeighbourOfBall(tempVec)) {
					positions.add(tempVec);
				}
			}
		}
		
		return positions;
	}
	
	public LinkedList<Vector2i> getBlockingMoves(Player player) {
		return filterBlockingMoves(getPossibleMoves(), player);
	}
	
	public LinkedList<Vector2i> filterBlockingMoves(LinkedList<Vector2i> moves, Player player) {
		LinkedList<Vector2i> blockingMoves = new LinkedList<Vector2i>();
		
		Vector2i nextPos;
		for (Vector2i move : moves) {
			for (int i = Vector2i.Direction.MIN_INT; i < Vector2i.Direction.MAX_INT; i++) {
				nextPos = getNextPosition(move, player, i);
				if (nextPos != null) {
					blockingMoves.add(move);
				}
			}
		}
		
		return blockingMoves;
	}
	
	public LinkedList<Vector2i> getMoveSuggestions() {
		// getpossiblemoves
		// filterblockingmoves
		// filterpossiblemoves.size() == 0 ? return filterpossiblemoves : getpossiblemoves;
		return null;
	}
	
	// TODO
	public boolean isCapturing(Move move) {
		
		return false;
	}
	
	// TODO
	public boolean isMoveAllowed(Move move) {
		return false;
	}
	
	public String toString() {
		String res = new String();
		for (int y = 0; y < BOARD_H; y++) {
			res += "|";
			for (int x = 0; x < BOARD_H; x++) {
				if (fields[x][y] == null) {
					res +=  "      |";
				} else {
					res += fields[x][y].getColour().toNormalisedString() + "|";
				} 
			}
			
			res += "\n";
		}
		
		return res;
	}
	
	public static void main(String[] args) {
		BoardModel board = new BoardModel();
		Player plr = new Player(Player.Colour.Blue, "Ruben XII");
		
		Move m1 = new Move(new Vector2i(0, 0), plr);
		Move m2 = new Move(new Vector2i(4, 0), plr);
		Move m3 = new Move(new Vector2i(4, 4), plr);
		
		board.applyMove(m1);
		board.applyMove(m2);
		board.applyMove(m3);
		
		System.out.println(board.toString());
	}
}
