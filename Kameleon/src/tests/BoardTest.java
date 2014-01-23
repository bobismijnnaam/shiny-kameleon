package tests;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import players.Player;
import utility.RatioPanel;
import board.BoardController;
import board.BoardModel;
import board.BoardView;

public class BoardTest extends JFrame {

	public static void main(String[] args) throws IOException {
		BoardModel board = new BoardModel();
		Player[] players = new Player[4];
		players[0] = new Player(Player.Colour.Red, "Ruben XII");
		players[1] = new Player(Player.Colour.Yellow, "Ruben XII");
		players[2] = new Player(Player.Colour.Green, "Ruben XII");
		players[3] = new Player(Player.Colour.Blue, "Ruben XII");
		board.setStartPosition(players[0], players[1], players[2], players[3]);
		
		JFrame mainFrame = new JFrame();
		Insets mainInsets = mainFrame.getInsets();
		BoardView mainView = new BoardView(board);
		mainFrame.setTitle("ROLLIT RUB");
		mainFrame.add(mainView.getRootPane());
		mainFrame.setSize(600, 620);
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainView.setStartPosition();

		BoardController controller = new BoardController(board, mainView.getFieldButtons(),
				mainView.getFields(), players);
		controller.startPlayerTurn(players[0]);
		mainView.addListeners(controller);
		mainView.setVisible(true);
		
		controller.startPlayerTurn(players[0]);
	}

}
