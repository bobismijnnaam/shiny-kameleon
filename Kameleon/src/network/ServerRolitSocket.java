package network;

import java.util.Arrays;
import java.util.List;

import leaderboard.Score;
import utility.Utils;

// TODO: More JML-like specifications (like checking if player != null, etc.)
// TODO: Check lobby limitations and shit

public class ServerRolitSocket extends RolitSocket {
	public ServerRolitSocket(int port) {
		super(port);
	}
	
	public void tellLEAVE(String player) {
		sendMsg("LEAVE " + player);
	}
	
	public void askVSIGN(String challenge) {
		sendMsg("VSIGN " + challenge);
	}
	
	public void tellSTATE(PlayerState state) {
		sendMsg("STATE " + state.toString());
	}
	
	public void tellSTART(String... players) {
		if (players.length > 4 || players.length == 0) {
			throw new IllegalArgumentException("Illegal Argument Exception:"
					+ "Can only pass between 1 and 4 players");
		}
		
		sendMsg("START " + Utils.join(Arrays.asList(players), " "));
	}
	
	public void tellGTURN(int player) {
		if (player > 3 || player < 0) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "player must be between 0 and 3");
		}
		
		sendMsg("GTURN " + Integer.toString(player));
	}
	
	public void tellGMOVE(int player, int x, int y) {
		if (x > 7 || x < 0 || y > 7 || y < 0) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "x & y must both be between 0 & 7 inclusive");
		}
		
		sendMsg("GMOVE " + Integer.toString(x) + " " + Integer.toString(y));
	}
	
	public void tellBOARD(int[] board) {
		if (board.length != 64) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "length of board must be 64");
		}
		
		sendMsg("BOARD " + Utils.join(Arrays.asList(board), " "));
	}
	
	public void tellGPLST(String... players) {
		sendMsg("GPLST " + Utils.join(Arrays.asList(players), " "));
	}
	
	public void tellBCAST(String msg) {
		sendMsg("BCAST " + msg);
	}
	
	// TODO: Make Score.toString actually work!
	public void tellScore(List<Score> scores) {
		sendMsg("SCORE " + Utils.join(scores, " "));
	}
	
	public void tellPLIST(String... players) {
		sendMsg("PLIST " + Utils.join(Arrays.asList(players), " "));
	}
	
	public void tellLJOIN(String player) {
		sendMsg("LJOIN " + player);
	}
	
	public void askINVIT(String... players) {
		if (players.length == 0 || players.length > 3) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "you must at least invite 1 and can invite up to 3 players "
					+ "(the guy who invited + three participants).");
		}
		
		sendMsg("INVIT R " + Utils.join(Arrays.asList(players), " "));
	}
	
	public void tellINVIT() {
//		if (status == INVITStatus.Denied || status == INVITStatus)
//		sendMsg("INVIT " + status.toString());
		sendMsg("INVIT " + INVITStatus.Denied.toString());
		
		// The reasoning behind this:
		// First, the server can't tell a client that he accepts or denies
		// an invite since it can't participate.
		// Second, suggesting a game should be done through the askINVIT funtion
		// Therefore, the tellINVIT may only send an invitation failed message.
		// (Since there are effectively no other values left)
	}
	
	public void tellCHATM(String player, String msg) {
		sendMsg("CHATM " + player + " " + msg);
	}
}
;