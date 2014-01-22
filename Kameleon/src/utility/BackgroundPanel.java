package utility;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel{
	
	private ImageIcon texture; 
	
	/**
	 * Constructs a panel width a image background.
	 * @param path - path to the background image
	 */
	public BackgroundPanel(String path) {
		texture = new ImageIcon(path);
	}
	
	/**
	 * Override the paintComponent to draw textures equal to jPanel width and height.
	 */
	@Override
	public void paintComponent(Graphics g) {
		Dimension d = getSize();
		g.drawImage(texture.getImage(), 0, 0, d.width, d.height, null);
	}
	
	public void changeTexture(String path) {
		texture = new ImageIcon(path);
		Graphics g = getGraphics();
		Dimension d = getSize();
		g.drawImage(texture.getImage(), 0, 0, d.width, d.height, null);
	}
}
