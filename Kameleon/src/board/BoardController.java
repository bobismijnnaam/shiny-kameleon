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
	
	public BoardController(BoardModel inputBoard, JButton[][] inputFieldButtons, 
			BackgroundPanel[][] inputFields) {
		
		board = inputBoard;
		fieldButtons = inputFieldButtons;
		fields = inputFields;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton change = (JButton) e.getSource();
		String name = change.getName();
		String[] split = name.split("-");
		Vector2i position = new Vector2i(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
		Player plr1 = new Player(Player.Colour.Red, "Ruben XII");
		Player plr2 = new Player(Player.Colour.Green, "Ruben XII");
		Player plr3 = new Player(Player.Colour.Blue, "Ruben XII");
		Player plr4 = new Player(Player.Colour.Yellow, "Ruben XII");
		board.setStartPosition(plr1, plr2, plr3, plr4);
		Move m1 = new Move(position, plr2);
		board.applyMove(m1);
		System.out.println(board.toString());
	}

}
