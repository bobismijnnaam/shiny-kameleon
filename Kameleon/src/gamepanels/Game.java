package gamepanels;

import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame {
	
	public final int STATE_MAIN = 0;
	public final int STATE_OFFLINE = 1;
	public final int STATE_ONLINE = 2;
	
	private JPanel currentState;

	public Game() {
		// TODO Auto-generated constructor stub
	}
	
	public void setNextState(int nextState) {
		switch (nextState) {
		case STATE_MAIN:
			currentState = null;
			currentState = new MainMenu(this);
			break;
		case STATE_OFFLINE:
			break;
		case STATE_ONLINE:
			break;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Game gm = new Game();
		gm.setNextState(STATE_MAIN);
	}

}
