package utility;

import javax.swing.JPanel;

public class RatioPanel extends JPanel {
	
	private static final long serialVersionUID = -2573028994604538252L;

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, Math.min(width, height), Math.min(width, height));
	}
	
}
