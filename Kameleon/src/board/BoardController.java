package board;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import utility.BackgroundPanel;
import utility.Move;
import utility.Player;
import utility.Vector2i;

public class BoardController implements ActionListener {
	
	private BoardModel board;
	private JButton[][] fieldButtons;
	private BackgroundPanel[][] fields;
	private Player[] players;
	
	public BoardController(BoardModel inputBoard, JButton[][] inputFieldButtons, 
			BackgroundPanel[][] inputFields, Player[] inputPlayers) {
		
		board = inputBoard;
		fieldButtons = inputFieldButtons;
		fields = inputFields;
		players = inputPlayers;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton change = (JButton) e.getSource();
		String name = change.getName();
		String[] split = name.split("-");
		Vector2i position = new Vector2i(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		Move m1 = new Move(position, players[3]);
		board.applyMove(m1);
		System.out.println(board.toString());
	}

}
