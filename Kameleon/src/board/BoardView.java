package board;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.*;

import utility.BackgroundPanel;
import utility.Player;
import utility.Player.Colour;
import utility.Vector2i;

public class BoardView extends JFrame implements Observer {
	
	// create array for the field images and buttons
	private BackgroundPanel[][] fields;
	private JButton[][] fieldButtons;
	private JButton button;
	private BoardModel board;
	
	public BoardView(BoardModel inputBoard) throws IOException {
		makeGUI(inputBoard);
	}
	
	public void makeGUI(BoardModel inputBoard) throws IOException {
		
		// initialize arrays
		fields = new BackgroundPanel[8][8];
		fieldButtons = new JButton[8][8];
		board = inputBoard;
		
		// the board container
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		add(container);
		
		// adds the x row 8 times
		for (int y = 0; y < 8; y++) {
			// creates an xRow
			JPanel xRow = new JPanel();
			xRow.setLayout(new BoxLayout(xRow, BoxLayout.X_AXIS));
			
			// create the y row
			for (int x = 0; x < 8; x++) {
				// make the button transparent
				button = new JButton();
				button.setName("" + x + "-" + y);
				button.setOpaque(false);
				button.setContentAreaFilled(false);
				button.setBorderPainted(false);
				BackgroundPanel empty = new BackgroundPanel("media/empty.png"); //set up panel
				empty.setLayout(new BorderLayout());
				empty.add(button, BorderLayout.CENTER);
				
				fieldButtons[x][y] = button;
				fields[x][y] = empty;
				xRow.add(empty);
			}
			
			// set the preferred y row size
			xRow.setPreferredSize(new Dimension(640, 80));
			// xRow creation done
			container.add(xRow);
		}
		
		// set the complete preferred board size
		container.setPreferredSize(new Dimension(640, 640));
		setTitle("ROLLIT RUB");
		pack();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// set the initial fields
		fields[3][3].changeTexture("media/red.png");
		fields[4][3].changeTexture("media/green.png");
		fields[3][4].changeTexture("media/blue.png");
		fields[4][4].changeTexture("media/yellow.png");
		// disable the buttons
		fieldButtons[3][3].setEnabled(false);
		fieldButtons[4][3].setEnabled(false);
		fieldButtons[3][4].setEnabled(false);
		fieldButtons[4][4].setEnabled(false);
		
		// Add Board controller
		//BoardController controller = new BoardController(board, fieldButtons, fields);
		
		/*// set action listeners
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				fieldButtons[i][j].addActionListener(controller);
			}
		} */
		
		// Add board observer
		board.addObserver(this);
	}
	
	public void addListeners(BoardController inputController) {
		// set action listeners
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				fieldButtons[i][j].addActionListener(inputController);
			}
		}
	}
	
	public BackgroundPanel[][] getFields() {
		return fields;
	}
	
	/*
	public static void main(String[] args) throws IOException {
		BoardModel board = new BoardModel();
		Player plr1 = new Player(Player.Colour.Red, "Ruben XII");
		Player plr2 = new Player(Player.Colour.Green, "Ruben XII");
		Player plr3 = new Player(Player.Colour.Blue, "Ruben XII");
		Player plr4 = new Player(Player.Colour.Yellow, "Ruben XII");
		board.setStartPosition(plr1, plr2, plr3, plr4);
		BoardView mainView = new BoardView(board);
		mainView.setVisible(true);
	}
	
	*/
	
	public JButton[][] getFieldButtons() {
		return fieldButtons;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				Player player = board.getPlayerAt(x, y);
				if (player == null) {
					
				} else {
					Colour colour = player.getColour();
					if (colour == Player.Colour.Blue) {
						fields[x][y].changeTexture("media/blue.png");
						fieldButtons[x][y].setEnabled(false);
					} else if (colour == Player.Colour.Green) {
						fields[x][y].changeTexture("media/green.png");
						fieldButtons[x][y].setEnabled(false);
					} else if (colour == Player.Colour.Yellow) {
						fields[x][y].changeTexture("media/yellow.png");
						fieldButtons[x][y].setEnabled(false);
					} else if (colour == Player.Colour.Red) {
						fields[x][y].changeTexture("media/red.png");
						fieldButtons[x][y].setEnabled(false);
					}
				}
			}
		}
	}

}
