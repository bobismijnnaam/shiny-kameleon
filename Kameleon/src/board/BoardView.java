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

public class BoardView extends JFrame implements Observer {
	
	// create array for the field images and buttons
	private BackgroundPanel[][] fields;
	private JButton[][] fieldButtons;
	private JButton button;
	
	public BoardView() throws IOException {
		makeGUI();
	}
	
	public void makeGUI() throws IOException {
		
		// initialize arrays
		fields = new BackgroundPanel[8][8];
		fieldButtons = new JButton[8][8];
		
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
				button = new JButton("");
				button.setOpaque(false);
				button.setContentAreaFilled(false);
				button.setBorderPainted(true);
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
	}
	
	public static void main(String[] args) throws IOException {
		BoardView mainView = new BoardView();
		mainView.setVisible(true);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
