package network;

import java.util.Arrays;

import utility.Utils;

// TODO: Check lobby limitations and shit

public class ClientRolitSocket extends RolitSocket {
	public enum NGAMEFlags {
//		Challenge,
		Default,
		TwoPlayerGame, 
		ThreePlayerGame,
		FourPlayerGame;
		
		private String s;
		
		static {
//			Challenge.s = "C";
			Default.s = "D";
			TwoPlayerGame.s = "H";
			ThreePlayerGame.s = "I";
			FourPlayerGame.s = "J";
		}
		
		public String toString() {
			return new String(s);
		}
	}
	
	public ClientRolitSocket(String addr, int port) {
		super(addr, port);
	}
	
	// Doesn't need a tellLeave() method, since the RolitSocket class does this automatically
	
	public void askLOGIN(String username) {
		sendMsg("LOGIN " + username);
	}
	
	public void tellVSIGN(String plain, String key) {
		String signature = PKISocket.signMessage(plain, key);
		System.out.println("Signature: " + signature);
		sendMsg("VSIGN " + signature);
	}
	
	public void askSTATE() {
		sendMsg("STATE");
	}
	
	public void askNGAME(NGAMEFlags flag) {
		sendMsg("NGAME " + flag.toString());
	}
	
	/**
	 * 
	 * @param player
	 * @deprecated Use {@link ClientRolitSocket#askINVIT(String...)} instead.
	 */
	public void askNGAME(String player) {
		sendMsg("NGAME C " + player);
	}
	
	public void tellGMOVE(int x, int y) {
		if (x < 0 || x > 7 || y < 0 || y > 7) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "x & y must both be between 0 and 7 inclusive");
		}
		
		sendMsg("GMOVE " + Integer.toString(x) + " " + Integer.toString(y));
	}
	
	public void askBOARD() {
		sendMsg("BOARD");
	}
	
	public void askGPLST() {
		sendMsg("GPLST");
	}
	
	public void askSCORE(String playerName, int amount) {
		sendMsg("SCORE PLAYER " + playerName + " " + Integer.toString(amount));
	}
	
	public void askSCORE(int amount) {
		sendMsg("SCORE HIGH " + Integer.toString(amount));
	}
	
	public void askSCORE(int amount, ScoreTime time) {
		sendMsg("SCORE TIME " + Integer.toString(amount) + " " + time.toString());
	}
	
	public void askPLIST() {
		sendMsg("PLIST");
	}
	
	public void askINVIT(String... players) {
		if (players.length == 0 || players.length > 3) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "you must at least invite 1 and can invite up to 3 players "
					+ "(the guy who invited + three participants).");
		}
		
		sendMsg("INVIT R " + Utils.join(Arrays.asList(players), " "));
	}
	
	public void tellINVIT(INVITStatus status) {
		if (status == INVITStatus.Failed) {
			throw new IllegalArgumentException("Illegal argment exception: "
					+ "you can't tell the server \"Failed\".");
		}
		
		sendMsg("INVIT " + status.toString());
	}
	
	public void tellCHATM(String msg) {
		sendMsg("CHATM " + msg);
	}
	
	public static void main(String[] args) {
		ClientRolitSocket crs = new ClientRolitSocket("localhost", Server.SERVER_PORT);
		
		System.out.println("Let's do this!");
		crs.start();
		
		while (!crs.isConnected()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Oops, something went wrong while waiting!");
			}
		}
		
		int amountIn = 0;
		int amountOut = 0;
		
		// TEST ALL THE COMMANDS!
		crs.askLOGIN("client");
		// crs.tellVSIGN()
		crs.askSTATE();
		crs.askNGAME("ruben");
		crs.tellGMOVE(5, 4);
		crs.askBOARD();
		crs.askGPLST();
		crs.askSCORE(2);
		crs.askPLIST();
		crs.askINVIT("bob");
		crs.tellINVIT(INVITStatus.Accept);
		crs.tellCHATM("HELLO WORLD");
		
		amountOut = 11; 
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("oops");
		}
		
		// Process all the commands... :(
		while (crs.isNewMsgQueued()) {
			amountIn++;
			System.out.print("Incoming command: ");
			switch (crs.getQueuedMsgType()) {
				case AL_STATE:
					Utils.disp("STATE", crs.getQueuedMsgArray());
					break;
				case LO_INVIT:
					Utils.disp("INVIT", crs.getQueuedMsgArray());
					break;
				case IG_GMOVE:
					Utils.disp("GMOVE", crs.getQueuedMsgArray());
					break;
				case IG_BOARD:
					Utils.disp("BOARD", crs.getQueuedMsgArray());
					break;
				case IG_GPLST:
					Utils.disp("GPLST", crs.getQueuedMsgArray());
					break;
				case AL_SCORE:
					Utils.disp("SCORE", crs.getQueuedMsgArray());
					break;
				case LO_PLIST:
					Utils.disp("PLIST", crs.getQueuedMsgArray());
					break;
				default:
					System.out.print("you missed one :( : ");
					System.out.print(crs.getQueuedMsgType().toString());
					break;
			}
		}
		
		System.out.println("In: " + amountIn + " out: " + amountOut);
		
		crs.close();
		
		System.out.println("Done. Cya!");
	}
}
