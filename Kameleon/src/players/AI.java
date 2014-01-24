package players;

import board.BoardModel;
import utility.Vector2i;

public interface AI {
	Vector2i getMove(BoardModel board);
}
