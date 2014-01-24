package gamepanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class MainMenu extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton[] buttons;
	private String[] settings;
	JTextField username;
	JPasswordField password;
	JTextField server;
	JTextField port;
	
	public MainMenu(Game inputGame) {
		createMenu(inputGame);
	}
	
	public void createMenu(Game inputGame) {
		buttons = new JButton[20];
		settings = new String[4];
		setLayout(new MigLayout());
		JButton playOffline = new JButton("PLAY OFFLINE");
		JButton playOnline = new JButton("PLAY ONLINE");
		playOffline.setName("offline");
		playOnline.setName("online");
		MainMenuController mController = new MainMenuController(inputGame, buttons);
		add(playOffline, "span, growx, width 100%, height 25%");
		
		// create the disable player buttons
		for (int x = 0; x < 4; x++) {
			JButton disable = new JButton("Disable Player " + (x + 1));
			buttons[x] = disable;
			disable.setName("disable-" + x);
			disable.addActionListener(mController);
			if (x == 0) {
				add(disable, "span, split 4, width 25%, height 8%");
			} else {
				add(disable, "width 25%, height 8%");
			}
		}
		
		// create the human player buttons
		for (int x = 0; x < 4; x++) {
			JButton human = new JButton("Human Player " + (x + 1));
			buttons[x + 4] = human;
			human.setName("human-" + x);
			human.setEnabled(false);
			human.addActionListener(mController);
			if (x == 0) {
				add(human, "span, split 4, width 25%, height 8%");
			} else {
				add(human, "width 25% , height 8%");
			}
		}
		
		// create the easy computer buttons
		for (int x = 0; x < 4; x++) {
			JButton easy = new JButton("Computer Easy " + (x + 1));
			buttons[x + 8] = easy;
			easy.setName("easy-" + x);
			easy.addActionListener(mController);
			if (x == 0) {
				add(easy, "span, split 4,  width 25%, height 8%");
			} else {
				add(easy, " width 25%, height 8%");
			}
		}
		
		// create the medium computer buttons
		for (int x = 0; x < 4; x++) {
			JButton medium = new JButton("Computer Medium " + (x + 1));
			buttons[x + 12] = medium;
			medium.setName("medium-" + x);
			medium.addActionListener(mController);
			if (x == 0) {
				add(medium, "span, split 4,  width 25%, height 8%");
			} else {
				add(medium, " width 25%, height 8%");
			}
		}
		
		// create the medium computer buttons
		for (int x = 0; x < 4; x++) {
			JButton hard = new JButton("Computer hard " + (x + 1));
			buttons[x + 16] = hard;
			hard.setName("hard-" + x);
			hard.addActionListener(mController);
			if (x == 0) {
				add(hard, "span, split 4,  width 25%, height 8%");
			} else {
				add(hard, " width 25%, height 8%");
			}
		}
		
		add(playOnline, "span, growx, width 100%, height 25%");
		
		// textfield
		username = new JTextField("username");
		password = new JPasswordField("password");
		server = new JTextField("server");
		port = new JTextField("port");
		add(username, "span, split 4, height 5%, width 25%");
		add(password, "height 5%, width 25%");
		add(server, "height 5%, width 25%");
		add(port, "height 5%, width 25%");

		playOffline.addActionListener(mController);
		playOnline.addActionListener(mController);
	}
	
	// controller
	public class MainMenuController implements ActionListener {

		private Game currentGame;
		private JButton[] buttons;
		
		public MainMenuController(Game inputGame, JButton[] inputButtons) {
			currentGame = inputGame;
			buttons = inputButtons;
			
			for (int x = 0; x < 4; x++) {
				settings[x] = "human";
			}
		}
		
		public void enableRow(int i) {
			if (i == 0) {
				buttons[i].setEnabled(true);
				buttons[i + 4].setEnabled(true);
				buttons[i + 8].setEnabled(true);
				buttons[i + 12].setEnabled(true);
				buttons[i + 16].setEnabled(true);
			} else if (i == 1) {
				buttons[i].setEnabled(true);
				buttons[i + 4].setEnabled(true);
				buttons[i + 8].setEnabled(true);
				buttons[i + 12].setEnabled(true);
				buttons[i + 16].setEnabled(true);
			} else if (i == 2) {
				buttons[i].setEnabled(true);
				buttons[i + 4].setEnabled(true);
				buttons[i + 8].setEnabled(true);
				buttons[i + 12].setEnabled(true);
				buttons[i + 16].setEnabled(true);
			} else if (i == 3) {
				buttons[i].setEnabled(true);
				buttons[i + 4].setEnabled(true);
				buttons[i + 8].setEnabled(true);
				buttons[i + 12].setEnabled(true);
				buttons[i + 16].setEnabled(true);
			}
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton check = (JButton) e.getSource();
			String id = " ";
			int i = 0;
			if (check.getName() == "online") {
				settings[0] = username.getText();
				settings[1] = new String(password.getPassword());
				settings[2] = server.getText();
				settings[3] = port.getText();
				try {
					currentGame.setNextState(Game.STATE_ONLINE, settings);
				} catch (IOException ie) {
					System.out.println("Game State couldn't be changed");
				}
			} else if (check.getName() == "offline") {
				try {
					currentGame.setNextState(Game.STATE_OFFLINE, settings);
				} catch (IOException ie) {
					System.out.println("Game State couldn't be changed");
				}
			} else {
				id = check.getName();
				System.out.println(id);
				if (id.equals("disable-2")) {
					if (!buttons[3].isEnabled()) {
						String[] parts = id.split("-");
						i = Integer.parseInt(parts[1]);
						
						settings[i] = parts[0];
						enableRow(i);
						check.setEnabled(false);
					}
				} else if (id.equals("disable-1") || id.equals("disable-0")) { 
					// nothing 
				} else {
					String[] parts = id.split("-");
					i = Integer.parseInt(parts[1]);
					
					if (i == 3) {
						if (!check.getName().equals("disable-3") && !buttons[2].isEnabled()) {
							buttons[2].setEnabled(true);
							buttons[6].setEnabled(false);
						}
					}
					
					settings[i] = parts[0];
					
					enableRow(i);
					check.setEnabled(false);
				}
			}
		}

	}
}
