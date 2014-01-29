package board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import players.Player;
import utility.*;

public class BoardModel extends Observable {
	private Player[][] fields;
	
	public static final int BOARD_W = 8;
	public static final int BOARD_H = 8;
	
	private Player currentPlayer;
	
	private LinkedList<AffectedPosition> affectedByMove;
	private boolean newPositionsAffected = false;

	private Player[] players;
	
	public class AffectedPosition {
		public Vector2i position;
		public Player startPlayer;
		public Player endPlayer;
		
		public AffectedPosition(Vector2i inputPosition,
				Player inputStartPlayer, Player inputEndPlayer) {
			position = inputPosition;
			startPlayer = inputStartPlayer;
			endPlayer = inputEndPlayer;
		}
	}
	
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
	 * @return A LinkedList<Vector2i> containing all the affected fields
	 */
	public LinkedList<Vector2i> applyMove(Move move) {
		Vector2i p = move.getPosition();
		
		affectedByMove = new LinkedList<AffectedPosition>();
		affectedByMove.add(new AffectedPosition(new Vector2i(p), 
				fields[p.x][p.y], move.getPlayer()));
	
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
					affectedByMove.add(new AffectedPosition(new Vector2i(inbetweenPos), 
							fields[inbetweenPos.x][inbetweenPos.y], move.getPlayer()));
					fields[inbetweenPos.x][inbetweenPos.y] = move.getPlayer();
					inbetweenPos = inbetweenPos.getNeighbour(i);
				}
				
				affectedByMove.add(new AffectedPosition(new Vector2i(inbetweenPos), 
						fields[inbetweenPos.x][inbetweenPos.y], move.getPlayer()));
				fields[inbetweenPos.x][inbetweenPos.y] = move.getPlayer();
				
			}
		}
		
        setChanged();
        notifyObservers();
        
        return null;
	}
	
	/**
	 * Returns a LinkedList<AffectedPositions> of positions which where affected by the last move.
	 * This can only be queried directly after a move was made.
	 * As soon as this function is executed, isNewAffectedList() will return false again.
	 * This function can however still be queried.
	 * @return
	 */
	public LinkedList<AffectedPosition> getAffectedPositions() {
		newPositionsAffected = false;
		return affectedByMove;
	}
	
	/**
	 * Returns whether there is a new list of affected positions that has not yet ben queried.
	 * @return True if there is an unqueried list, false if not.
	 */
	public boolean isNewAffectedList() {
		return newPositionsAffected;
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
		Vector2i nextPos = p.getNeighbour(dir);
		
		while (containsPosition(nextPos) 
				&& getPlayerAt(nextPos) != null && getPlayerAt(nextPos) != player) {
			nextPos = nextPos.getNeighbour(dir);
		}
			
		if (!containsPosition(nextPos)) {
			return null;
		} else if (getPlayerAt(nextPos) == null) {
			return null;
		} else if (nextPos.isNeighbour(p)) {
			return null;
		} else {
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
	 * Gets the player occupying the neighbouring field in direction dir.
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
			if (containsPosition(p.getNeighbour(i)) && getNeighbour(p, i) != null) { 
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
				if (isNeighbourOfBall(tempVec) && getPlayerAt(tempVec) == null) {
					positions.add(tempVec);
				}
			}
		}
		
		return positions;
	}
	
	/**
	 * Gives all positions which capture fields and block players if that field.
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
					break;
				}
			}
		}
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
		
		if (blockingMoves.size() == 0) {
			return possibleMoves;
		} else {
			return blockingMoves;
		}
	}
	
	/**
	 * Returns whether a move captures and blocks other players.
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
	 * Returns a list length zero, one, or two, depending on how much winners there are.
	 * @return A Player[] filled with the winners.
	 */
	public Player[] getWinners() {
		if (isGameOver()) {
			// Gather players in the game
			ArrayList<Player> gPlayers = new ArrayList<Player>(4);
			for (int y = 0; y < BOARD_H; y++) {
				for (int x = 0; x < BOARD_W; x++) {
					if (!gPlayers.contains(fields[x][y]) && fields[x][y] != null) {
						gPlayers.add(fields[x][y]);
					}
					
					if (gPlayers.size() == 4) {
						break;
					}
				}
				
				if (gPlayers.size() == 4) {
					break;
				}
			}
			
			// Gather score for each present player
			int[] scores = new int[gPlayers.size()];
			for (int i = 0; i < scores.length; i++) {
				scores[i] = getScore(gPlayers.get(i));
			}
			
			int highscore = 0;
			Player highplayer = null;
			for (int i = 0; i < scores.length; i++) {
				if (scores[i] > highscore) {
					highscore = scores[i];
					highplayer = gPlayers.get(i);
				}
			}
			
			// Check for equal score
			boolean sharedFirstPlace = false;
			Player otherWinner = null;
			for (int i = 0; i < scores.length; i++) {
				if (scores[i] == highscore && gPlayers.get(i) != highplayer) {
					sharedFirstPlace = true;
					otherWinner = gPlayers.get(i);
				}
			}
			
			// Set the two winning players as winners in the array
			Player[] winners;
			if (sharedFirstPlace) {
				winners = new Player[2];
				winners[1] = otherWinner;
			} else { // Or just one
				winners = new Player[1];
			}
			winners[0] = highplayer;
			
			// Return the result;
			return winners;
		} else {
			return new Player[0];
		}
	}
	
	/**
	 * Returns whether there is a winner.
	 * @param p - The player to check the winstate for
	 * @return True or false
	 */
	public boolean isWinner(Player p) {
		Player[] winners = getWinners();
		for (int i = 0; i < winners.length; i++) {
			if (winners[i] == p) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return True if there is a winner.
	 */
	public boolean hasWinner() {
		return getWinners().length > 0;
	}
	
	/**
	 * @return True if the game is over.
	 */
	public boolean isGameOver() {
		for (int y = 0; y < BOARD_H; y++) {
			for (int x = 0; x < BOARD_W; x++) {
				if (fields[x][y] == null) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void setPlayers(Player[] inputPlayers) {
		players = inputPlayers;
	}
	
	public Player[] getPlayers() {
		return players;
	}
	
	public Player getNextPlayer(Player p) {
		for (int i = 0; i < players.length; i++) {
			if (p == players[i]) {
				int pos = (i + 1) % players.length;
				return players[pos];
			}
		}
		
		return p;
	}
	
	/**
	 * Returns a string representatoin of the current board.
	 */
	public String toString() {
		System.out.println("[Board] Printing board");
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
	
	public void setPosition(Player p, Vector2i pos) {
		fields[pos.x][pos.y] = p;
	}
	
	public BoardModel deepCopy() {
		BoardModel bm = new BoardModel();
		
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				bm.setPosition(fields[x][y], new Vector2i(x, y));
			}
		}
		
		bm.setPlayers(getPlayers());
		
		return bm;
	}
	
	public int getGrade(Player player) {		
		int[][] lt = new int[][]{
			{10000,	-3000, 1000, 800, 800, 1000, -3000, 10000},
			{-3000, -5000, -450, -500, -500, -450, -5000, -3000},
			{1000, -450, 30, 10, 10, 30, -450, 1000},
			{800, -500, 10, 50, 50, 10, -500, 800},
			{800, -500, 10, 50, 50, 10, -500, 800},
			{1000, -450, 30, 10, 10, 30, -450, 1000},
			{-3000, -5000, -450, -500, -500, -450, -5000, -3000},
			{10000, -3000, 1000, 800, 800, 1000, -3000, 10000},
		};
	
		
		if (hasWinner()) {
			if (isWinner(player)) {
				return 9999;
			} else {
				return -9999;
			}
		}
		
		int grade = 0;
		for (int x = 0; x < BOARD_W; x++) {
			for (int y = 0; y < BOARD_H; y++) {
				grade += lt[x][y];
			}
		}
		
		return grade;
		
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
}
