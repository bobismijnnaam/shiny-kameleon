package board;

import java.util.LinkedList;
import java.util.Observable;

import players.Player;
import utility.*;

public class BoardModel extends Observable {
	private Player[][] fields;
	
	public static final int BOARD_W = 8;
	public static final int BOARD_H = 8;
	
	private Player currentPlayer;
	
	/**
	 * Constructs a boardmodel instance with every field set to null.
	 */
	public BoardModel() {
		fields = new Player[BOARD_W][BOARD_H];
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				fields[x][y] = null;
			}
		}
		
		currentPlayer = null;
	}
	
	/**
	 * Initializes the board with one player. The other 3 colours will be generated anonymously.
	 * @param p1 - Player one.
	 * @see BoardModel#setStartPosition(Player, Player, Player, Player)
	 */
	public void setStartPosition(Player p1) {
		setStartPosition(p1, new Player(Player.Colour.Yellow, "Player 2"));
	}
	
	/**
	 * Initializes the board with two players. The other 2 colours will be generated anonymously.
	 * @param p1 - Player one.
	 * @param p2 - Player two.
	 * @see BoardModel#setStartPosition(Player, Player, Player, Player)
	 */
	public void setStartPosition(Player p1, Player p2) {
		setStartPosition(p1, p2, new Player(Player.Colour.Green, "Player 3"));
	}
	
	/**
	 * Initializes the board with three players. The remaining will be generated anonymously.
	 * @param p1 - Player one.
	 * @param p2 - Player two.
	 * @param p3 - Player three.
	 * @see BoardModel#setStartPosition(Player, Player, Player, Player)
	 */
	public void setStartPosition(Player p1, Player p2, Player p3) {
		setStartPosition(p1, p2, p3, new Player(Player.Colour.Blue, "Player 4"));
	}
	
	/**
	 * Initializes the board with four players. The board will use the references to the 
	 * players to recognize colors on the field and block/capture the balls inbetween.
	 * @param p1 - Player one.
	 * @param p2 - Player two.
	 * @param p3 - Player three.
	 * @param p4 - Player four.
	 */
	public void setStartPosition(Player p1, Player p2, Player p3, Player p4) {
		fields[3][3] = p1;
		fields[4][3] = p2;
		fields[3][4] = p4;
		fields[4][4] = p3;
		
		currentPlayer = p1;
		
        setChanged();
        notifyObservers();
	}
	
	/**
	 * Applies a move to the current field. After setting the position 
	 * in the field to the playercolor, it evaluates the move and
	 * connects the position to any other positions of the playercolor
	 * as described in the games rule document.
	 * @param move - The move to apply.
	 */
	public void applyMove(Move move) {
		Vector2i p = move.getPosition();
		fields[p.x][p.y] = move.getPlayer();
		
		Vector2i inbetweenPos;
		Vector2i nextPos;
		
		// Iterate over directions
		for (int i = Vector2i.Direction.MIN_INT; i <= Vector2i.Direction.MAX_INT; i++) {
			nextPos = getNextPosition(move.getPosition(), move.getPlayer(), i);
			
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
	
	/**
	 * @see BoardModel#getNextPosition(Vector2i, Player, int)
	 */
	public Vector2i getNextPosition(Vector2i p, Player player, Vector2i.Direction dir) {
		return getNextPosition(p, player, dir.toInt());
	}
	
	/**
	 * Returns the next position where there is a ball of player in a given direction.
	 * Technically this function tells you if the game will recoloring and where
	 * it will stop recoloring
	 * @param p - The start point of the check
	 * @param player - The player to look for on the field
	 * @param dir - The direction
	 * @return null if no playerball is found, or a vector to indicate an endpoint.
	 * @see Vector2i.Direction
	 */
	public Vector2i getNextPosition(Vector2i p, Player player, int dir) {
		String output = "Hunt start: " + p.toString() + "|";
		
		Vector2i nextPos = p.getNeighbour(dir);
		
		output += "Try: " + nextPos.toString() + " | ";
		
		while (containsPosition(nextPos) 
				&& getPlayerAt(nextPos) != null && getPlayerAt(nextPos) != player) {
			nextPos = nextPos.getNeighbour(dir);
			
			output += "Try: " + nextPos.toString() + " | ";
		}
		
		output += "Found? " + nextPos.toString() + " | ";
			
		if (!containsPosition(nextPos)) {
			return null;
		} else if (getPlayerAt(nextPos) == null) {
			return null;
		} else {
			System.out.println(output + "Found!");
			return nextPos;
		}
	}
	
	/**
	 * Calculates the score of a player.
	 * @param player - The player you want to know the score of.
	 * @return The score of said player.
	 */
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
	
	/**
	 * Gets the player which occupies field p.
	 * @param p - The place you want to know the occupier of.
	 * @return The player of the field. Returns null if field is empty.
	 */
	public Player getPlayerAt(Vector2i p) {
		return fields[p.x][p.y];
	}
	
	/**
	 * @see BoardModel#getNeighbour(Vector2i, int)
	 */
	public Player getNeighbour(Vector2i p, Vector2i.Direction dir) {
		return getNeighbour(p, dir.toInt());
	}
	
	/**
	 * Gets the player occupying the neighbouring field in direction dir
	 * @param p - The position you want to analyze a neighbour of
	 * @param dir - The direction you want to check
	 * @return null if empty, or a pointer to the player occupying the neighbouring cell
	 */
	public Player getNeighbour(Vector2i p, int dir) {
		Vector2i newPos = p.getNeighbour(dir);
		return getPlayerAt(newPos);
	}
	
	/**
	 * Checks whether given position is within the bounds of the field.
	 * @param p - The position you want to check
	 * @return True if within bounds, otherwise false.
	 */
	public boolean containsPosition(Vector2i p) {
		if (p == null) {
			return false;
		} else if (p.x < BOARD_W && p.y < BOARD_H && p.x >= 0 && p.y >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks whether a position has a ball as neighbour (and not an empty field).
	 * @param p - The position of which you want to check if it has a ball-neighbour
	 * @return True if it has a ballneighbour, otherwise false
	 */
	public boolean isNeighbourOfBall(Vector2i p) {
		for (int i = Vector2i.Direction.MIN_INT; i <= Vector2i.Direction.MAX_INT; i++) {
			if (containsPosition(p.getNeighbour(i)) && getNeighbour(p, i) != null) { // Has a guard for out of bounds protection
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns a list with all the possible moves. That is, positions
	 * that have an occupied field as a neighbour.
	 * @return A LinkedList<Vector2i> containing the viable positions
	 */
	public LinkedList<Vector2i> getPossibleMoves() {
		LinkedList<Vector2i> positions = new LinkedList<Vector2i>();
		Vector2i tempVec;
		
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				tempVec = new Vector2i(x, y);
				System.out.println(tempVec.toString());
				if (isNeighbourOfBall(tempVec) && getPlayerAt(tempVec) == null) {
					positions.add(tempVec);
				}
			}
		}
		
		return positions;
	}
	
	/**
	 * Gives all positions which capture fields and block players if that field
	 * would be occupied by said player 
	 * @see BoardModel#filterBlockingMoves(LinkedList, Player)
	 */
	public LinkedList<Vector2i> getBlockingMoves(Player player) {
		return filterBlockingMoves(getPossibleMoves(), player);
	}
	
	/**
	 * Gives all positons from a given list which capture fields and block players.
	 * if those positions are occupied by said player
	 * @param moves - The LinkedList<Vector2i> filled with moves
	 * @param player - The player to check for
	 * @return A list of moves which would capture and block fields if 
	 * occupied by said player. This list CAN be empty!
	 */
	public LinkedList<Vector2i> filterBlockingMoves(LinkedList<Vector2i> moves, Player player) {
		LinkedList<Vector2i> blockingMoves = new LinkedList<Vector2i>();
		
		Vector2i nextPos;
		for (Vector2i move : moves) {
			for (int i = Vector2i.Direction.MIN_INT; i <= Vector2i.Direction.MAX_INT; i++) {
				nextPos = getNextPosition(move, player, i);
				if (nextPos != null) {
					blockingMoves.add(move);
					System.out.println("Blocking move: " + nextPos.toString());
					break;
				}
			}
		}
		System.out.println("End of filtering blocking moves");
		return blockingMoves;
	}
	
	/**
	 * Gives a list of moves said player can make. The list automatically checks if there are
	 * moves where he blocks and captures other players. If there are moves that do this it
	 * only returns this move. If there are no moves like that, it just gives a list of moves 
	 * neighbouring an occupied (and thus valid) move.
	 * @param player - The player that wants to make a move
	 * @return A list of possible moves.
	 */
	public LinkedList<Vector2i> getMoveSuggestions(Player player) {
		LinkedList<Vector2i> possibleMoves = getPossibleMoves();
		LinkedList<Vector2i> blockingMoves = filterBlockingMoves(possibleMoves, player);
		
		System.out.println("blockingmoves size: " + blockingMoves.size());
		System.out.println(Boolean.toString(blockingMoves.size() == 0));
		
		if (blockingMoves.size() == 0) {
			System.out.println("No blockingmoves");
			return possibleMoves;
		} else {
			System.out.println("Blockingmoves!");
			return blockingMoves;
		}
	}
	
	/**
	 * Returns whether a move captures and blocks other players
	 * @param move - The move you want to check
	 * @return True if a move captures and blocks, otherwise false
	 */
	public boolean isCapturing(Move move) {
		for (int i = Vector2i.Direction.MIN_INT; i <= Vector2i.Direction.MAX_INT; i++) {
			if (getNextPosition(move.getPosition(), move.getPlayer(), i) != null) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks whether this move is allowed. If there are capturing and blocking moves available
	 * and you try to make a move that doesn't, it returns false.
	 * @param move - The move to check
	 * @return True if the move is allowed, otherwise false
	 */
	public boolean isMoveAllowed(Move move) {
		return getMoveSuggestions(move.getPlayer()).contains(move.getPosition());
	}
	
	/**
	 * Sets the current player.
	 * @param player - The current player
	 */
	public void setCurrentPlayer(Player player) {
		currentPlayer = player;
		
		 setChanged();
	     notifyObservers();
	}
	
	/**
	 * Returns the current player.
	 * @return The current player.
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
	}
	
	/**
	 * Returns a string representatoin of the current board.
	 */
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
	
	/**
	 * @return A deep copy to the fields 2D-Player array.
	 */
	public Player[][] deepBareCopy() {
		Player[][] copy = new Player[BOARD_W][BOARD_H];
		for (int y = 0; y < BOARD_H; y++) {
			for (int x = 0; x < BOARD_W; x++) {
				copy[x][y] = fields[x][y];
			}
		}
		
		return copy;
	}
	
	/**
	 * Test main
	 * @param args - fok joe
	 */
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
