package utility;

import players.Player;

public class Move {
	Vector2i position;
	Player player;
	
	/**
	 * Creates a move.
	 * @param inputPosition - The position you want to change
	 * @param inputPlayer - The color you want to change the position to
	 */
	public Move(Vector2i inputPosition, Player inputPlayer) {
		position = new Vector2i(inputPosition);
		player = inputPlayer;
	}
	
	/**
	 * Returns the position of the move.
	 * @return A new vector of the position of the move
	 */
	public Vector2i getPosition() {
		return new Vector2i(position);
	}
	
	/**
	 * Returns the player which performs the move.
	 * @return A pointer to the player.
	 */
	public Player getPlayer() {
		return player;
	}

}
