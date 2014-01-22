package tests;

import java.io.IOException;

import players.Player;
import board.BoardController;
import board.BoardModel;
import board.BoardView;

public class BoardTest {

	public static void main(String[] args) throws IOException {
		BoardModel board = new BoardModel();
		Player[] players = new Player[4];
		players[0] = new Player(Player.Colour.Red, "Ruben XII");
		players[1] = new Player(Player.Colour.Yellow, "Ruben XII");
		players[2] = new Player(Player.Colour.Green, "Ruben XII");
		players[3] = new Player(Player.Colour.Blue, "Ruben XII");
		board.setStartPosition(players[0], players[1], players[2], players[3]);
		BoardView mainView = new BoardView(board);

		BoardController controller = new BoardController(board, mainView.getFieldButtons(),
		mainView.getFields(), players);
		mainView.addListeners(controller);
		mainView.setVisible(true);
	}

}
