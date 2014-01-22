package utility;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class RatioPanel extends JPanel {

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, Math.min(width, height), Math.min(width, height));
	}
	
}
