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
		String signature = PKISocket.getSignature(plain, key);
		sendMsg("VSIGN " + signature);
	}
	
	public void askSTATE() {
		sendMsg("STATE");
	}
	
	public void askNGAME(NGAMEFlags flag) {
		sendMsg("NGAME " + flag.toString());
	}
	
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
		sendMsg("INVIT " + status.toString());
	}
	
	public void tellCHATM(String msg) {
		sendMsg("CHATM " + msg);
	}
}
