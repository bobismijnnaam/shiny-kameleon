package board;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import players.Player;
import players.Player.Colour;
import utility.BackgroundPanel;
import utility.RatioPanel;
import utility.Vector2i;

public class BoardView extends JInternalFrame implements Observer {
	
	private static final long serialVersionUID = 1L;
	// create array for the field images and buttons
	private BackgroundPanel[][] fields;
	private JButton[][] fieldButtons;
	private JButton button;
	private BoardModel board;
	
	// array is used to store the correct buttons
	private int arrayX, arrayY;
	
	/**
	 * Creates a new boardView.
	 * @param inputBoard the Board.
	 * @throws IOException
	 */
	public BoardView(BoardModel inputBoard) throws IOException {
		makeGUI(inputBoard);
	}
	
	/**
	 * Makes the GUI and adds them to the internalFrame.
	 * @param inputBoard The input Board.
	 * @throws IOException
	 */
	public void makeGUI(BoardModel inputBoard) throws IOException {
		BackgroundPanel bg = new BackgroundPanel("media/bg.png");
		bg.setLayout(new BorderLayout());
		// initialize arrays
		fields = new BackgroundPanel[8][8];
		fieldButtons = new JButton[8][8];
		board = inputBoard;
		
		// the board container a ratio Panel
		RatioPanel container = new RatioPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setOpaque(false);
		bg.add(container, BorderLayout.CENTER);
		bg.setPreferredSize(new Dimension(8000, 800));
		
		add(bg);
		
		// adds the x row 10 times
		for (int y = 0; y < 10; y++) {
			// creates an xRow
			JPanel xRow = new JPanel();
			xRow.setLayout(new BoxLayout(xRow, BoxLayout.X_AXIS));
			
			// create the y row
			for (int x = 0; x < 10; x++) {
				// if it's the left row
				if (x == 0 && y != 0 && y != 9) {
					
					// set button properties
					button = new JButton();
					button.setEnabled(false);
					button.setOpaque(false);
					button.setContentAreaFilled(false);
					button.setBorderPainted(false);
					
					// set standard gradient texture
					BackgroundPanel leftEdge = new BackgroundPanel("media/leftMid.png");
					
					// if it's the mid position set the color
					if (y == 4) {
						leftEdge = new BackgroundPanel("media/blueUp.png"); //set up panel
					} else if (y == 5) {
						leftEdge = new BackgroundPanel("media/blueDown.png"); //set up panel
					}
					
					// add the button and add everything up
					leftEdge.setLayout(new BorderLayout());
					leftEdge.add(button, BorderLayout.CENTER);
					xRow.setOpaque(false);
					xRow.add(leftEdge);
				}
				
				// make the button transparent
				if (x < 9 && y < 9 && x != 0 && y != 0) {
					
					// set the values to match array indexes
					arrayX = x - 1;
					arrayY = y - 1;
					
					// create a button set name 
					button = new JButton();
					button.setName("" + arrayX + "-" + arrayY);
					button.setOpaque(false);
					button.setContentAreaFilled(false);
					button.setBorderPainted(false);
					
					// set the texture
					BackgroundPanel empty = new BackgroundPanel("media/empty.png"); //set up panel
					empty.setLayout(new BorderLayout());
					empty.add(button, BorderLayout.CENTER);
					
					// add to the array
					fieldButtons[arrayX][arrayY] = button;
					fields[arrayX][arrayY] = empty;
					xRow.setOpaque(false);
					xRow.add(empty);
				}
				
				// if it's the right row
				if (x == 9 && y != 0 && y != 9) {
					
					// create a button to fix size
					button = new JButton();
					button.setEnabled(false);
					button.setOpaque(false);
					button.setContentAreaFilled(false);
					button.setBorderPainted(false);
					
					// set the default texture
					BackgroundPanel rightEdge = new BackgroundPanel("media/leftMid.png"); 
					
					// if it's the mid set the color
					if (y == 4) {
						rightEdge = new BackgroundPanel("media/yellowUp.png"); //set up panel
					} else if (y == 5) {
						rightEdge = new BackgroundPanel("media/yellowDown.png"); //set up panel
					}
					
					// add it
					rightEdge.setLayout(new BorderLayout());
					rightEdge.add(button, BorderLayout.CENTER);
					xRow.add(rightEdge);
					xRow.setOpaque(false);
				}
				
				// topRow
				if (y == 0) {
					
					// create button to fix height
					button = new JButton();
					button.setEnabled(false);
					button.setOpaque(false);
					button.setContentAreaFilled(false);
					button.setBorderPainted(false);
					
					// set default texture
					BackgroundPanel topBorder = new BackgroundPanel("media/topMid.png");
					
					// set the corners and colors
					if (x == 0) {
						topBorder = new BackgroundPanel("media/leftCorner.png"); //set up panel
					} else if (x == 9) {
						topBorder = new BackgroundPanel("media/rightCorner.png"); //set up panel
					} else if (x == 4) {
						topBorder = new BackgroundPanel("media/redLeft.png"); //set up panel
					} else if (x == 5) {
						topBorder = new BackgroundPanel("media/redright.png"); //set up panel
					}
					
					// add to row
					topBorder.setLayout(new BorderLayout());
					topBorder.add(button, BorderLayout.CENTER);
					xRow.setOpaque(false);
					xRow.add(topBorder);
				}
				
				// the bottom row
				if (y == 9) {
					
					// create button
					button = new JButton();
					button.setEnabled(false);
					button.setOpaque(false);
					button.setContentAreaFilled(false);
					button.setBorderPainted(false);
					
					// set default texture
					BackgroundPanel lowerBorder = new BackgroundPanel("media/lowerMid.png");
					
					// set corners and color
					if (x == 0) {
						lowerBorder = new BackgroundPanel("media/leftCorner.png"); //set up panel
					} else if (x == 9) {
						lowerBorder = new BackgroundPanel("media/rightCorner.png"); //set up panel
					} else if (x == 4) {
						lowerBorder = new BackgroundPanel("media/greenLeft.png"); //set up panel
					} else if (x == 5) {
						lowerBorder = new BackgroundPanel("media/greenright.png"); //set up panel
					}
					
					// add everything
					lowerBorder.setLayout(new BorderLayout());
					lowerBorder.add(button, BorderLayout.CENTER);
					xRow.setOpaque(false);
 					xRow.add(lowerBorder);
				}
			}
			
			// set the preferred y row size
			xRow.setPreferredSize(new Dimension(8000, 800));
			// xRow creation done
			container.add(xRow);
		}
		
		container.setPreferredSize(new Dimension(8000, 8000));
		// Add board observer
		board.addObserver(this);
		
	}

	/**
	 * Sets the startpositions in the View.
	 */
	public void setStartPosition() {
		// set the initial fields
		fields[3][3].changeTexture("media/red.png");
		fields[4][3].changeTexture("media/yellow.png");
		fields[3][4].changeTexture("media/blue.png");
		fields[4][4].changeTexture("media/green.png");
		// disable the buttons
		fieldButtons[3][3].setEnabled(false);
		fieldButtons[4][3].setEnabled(false);
		fieldButtons[3][4].setEnabled(false);
		fieldButtons[4][4].setEnabled(false);
		
		Player currentPlayer = board.getCurrentPlayer();
		LinkedList<Vector2i> moves = board.getMoveSuggestions(currentPlayer);
		for (Vector2i m : moves) {
			fields[m.x][m.y].changeTexture("media/enabled.png");
		}
	}
	
	/**
	 * Adds listeners to the Controller.
	 * @param inputController
	 */
	public void addListeners(BoardController inputController) {
		// set action listeners
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				fieldButtons[i][j].addActionListener(inputController);
			}
		}
	}
	
	/**
	 * Gets all the texturepanels.
	 * @return an array of backgroundPanels.
	 */
	public BackgroundPanel[][] getFields() {
		return fields;
	}
	
	/**
	 * Gets all the field buttons 8 * 8.
	 * @return an array of JButtons modeling the board.
	 */
	public JButton[][] getFieldButtons() {
		return fieldButtons;
	}

	/**
	 * Update gets called when notified by boardModel.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				Player player = board.getPlayerAt(new Vector2i(x, y));
				if (player == null) {
					fields[x][y].changeTexture("media/empty.png");
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
		
		Player currentPlayer = board.getCurrentPlayer();
		LinkedList<Vector2i> moves = board.getMoveSuggestions(currentPlayer);
		for (Vector2i m : moves) {
			fields[m.x][m.y].changeTexture("media/enabled.png");
		}
	}

}
