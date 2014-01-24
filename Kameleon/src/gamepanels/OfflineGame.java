package gamepanels;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import players.Player;
import utility.Move;
import utility.Vector2i;
import board.BoardController;
import board.BoardModel;
import board.BoardView;

public class OfflineGame extends JInternalFrame implements ActionListener {

	String[] settings;
	Player[] players;
	BoardModel board;
	BoardView mainView;
	BoardController boardController;
	int z = 0;
	int y = 0;
	public OfflineGame(String[] inputSettings) throws IOException {
		init(inputSettings);
	}
	
	public void init(String[] inputSettings) throws IOException {
		settings = inputSettings;
		
		System.out.println("Created a new board");
		board = new BoardModel();
		System.out.println("Created a new board view");
		mainView = new BoardView(board);
		add(mainView.getRootPane());
	}
	
	public void turnOver() {
		
	}
	
	public void setStartPosition() {
		players = new Player[4];
		players[0] = new Player(Player.Colour.Red, "Red");
		players[1] = new Player(Player.Colour.Yellow, "Yellow");
		players[2] = new Player(Player.Colour.Green, "Green");
		players[3] = new Player(Player.Colour.Blue, "Blue");
		
		y = 0;
		for (int i = 0; i < 4; i++) {
			if (settings[i].equals("human")) {
				y++;
			}
		}
		
		if (y == 2) {
			board.setStartPosition(players[0], players[1]);
		} else if (y == 3) {
			board.setStartPosition(players[0], players[1], players[2]);
		} else if (y == 4) {
			board.setStartPosition(players[0], players[1], players[2], players[3]);
		}
	}
	
	public void addListeners() {
		boardController = new BoardController(board, mainView.getFieldButtons(),
				mainView.getFields(), players);
		mainView.addListeners(boardController);
		mainView.addListeners(this);
		boardController.startPlayerTurn(players[0]);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		System.out.println("Received click on offlineGame");
		
		z++;
		if (z == y) {
			z = 0;
		}
		boardController.startPlayerTurn(players[z]);
		System.out.println("It's player" + z + "turn");
	}
}
