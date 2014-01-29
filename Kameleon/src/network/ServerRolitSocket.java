package network;

import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import leaderboard.Score;
import utility.Utils;

// TODO: More JML-like specifications (like checking if player != null, etc.)
// TODO: Check lobby limitations and shit

public class ServerRolitSocket extends RolitSocket {
	public ServerRolitSocket(Socket inputSock) {
		super(inputSock);
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
		
		sendMsg("GMOVE " + Integer.toString(player) + " "
				+ Integer.toString(x) + " " + Integer.toString(y));
	}
	
	public void tellBOARD(int[] board) {
		if (board.length != 64) {
			throw new IllegalArgumentException("Illegal Argument Exception: "
					+ "length of board must be 64");
		}
		
		sendMsg("BOARD " + Utils.join(board, " "));
	}
	
	public void tellGPLST(String... players) {
		sendMsg("GPLST " + Utils.join(Arrays.asList(players), " "));
	}
	
	public void tellBCAST(String msg) {
		sendMsg("BCAST " + msg);
	}
	
	// TODO: Make Score.toString actually work!
	public void tellSCORE(List<Score> scores) {
		
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
		//	if (status == INVITStatus.Denied || status == INVITStatus)
		//	sendMsg("INVIT " + status.toString());
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
	
	public static void main(String[] args) {
		
		// TODO: Make it function like it used to (just need to do something with
		// ServerSocket up here
		ServerRolitSocket srs = new ServerRolitSocket(null); // ya srs
		
		System.out.println("Let's do this!");
		srs.start();
		
		while (!srs.isConnected()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Something went wrong while waiting");
			}
		}
		
		int[] board = new int[64];
		for (int i = 0; i < 64; i++) {
			board[i] = i;
		}
		
		int amountIn = 0;
		int amountOut = 0;
		
		while (srs.isRunning()) {
			if (srs.isNewMsgQueued()) {
				amountIn++;
				System.out.print("Incoming command: ");
				switch (srs.getQueuedMsgType()) {
					case AC_VSIGN:
						Utils.disp("VSIGN", srs.getQueuedMsgArray());
						break;
					case AC_LOGIN:
						Utils.disp("LOGIN", srs.getQueuedMsgArray());
						break;
					case AL_CHATM:
						Utils.disp("CHATM", srs.getQueuedMsgArray());
						break;
					case AL_STATE:
						System.out.println("STATE:");
						srs.tellSTATE(PlayerState.PLAYING); amountOut++;
						srs.getQueuedMsgArray();
						break;
					case LO_NGAME:
						Utils.disp("NGAME", srs.getQueuedMsgArray());
						srs.askINVIT("ruben", "bob"); amountOut++;
						break;
					case IG_GMOVE:
						Utils.disp("GMOVE", srs.getQueuedMsgArray());
						srs.tellGMOVE(1, 5, 4); amountOut++;
						break;
					case IG_BOARD:
						System.out.println("BOARD");
						srs.getQueuedMsgArray();
						srs.tellBOARD(board); amountOut++;
						break;
					case IG_GPLST:
						System.out.println("GPLST");
						srs.getQueuedMsgArray();
						srs.tellGPLST("ruben", "bob"); amountOut++;
						break;
					case AL_SCORE:
						Utils.disp("SCORE", srs.getQueuedMsgArray());
						srs.tellSCORE(Arrays.asList(new Score[]{new Score("ruben", 100, null, null)
							, new Score("bob", 99, null, null)}));
						amountOut++;
						break;
					case LO_PLIST:
						System.out.println("PLIST");
						srs.getQueuedMsgArray();
						srs.tellPLIST("ruben", "bob"); amountOut++;
						break;
					case LO_INVIT:
						Utils.disp("INVIT", srs.getQueuedMsgArray());
						srs.askINVIT("ruben", "bob"); amountOut++;
						break;
					case AL_LEAVE:
						srs.close();
						break;
					case FB_ERROR:
						Utils.disp("ERROR", srs.getQueuedMsgArray());
						break;
					case X_NONE:
						System.out.println("none?!");
						break;
					default:
						System.out.print("you missed one :( : ");
						System.out.print(srs.getQueuedMsgType().toString());
						break;
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("Something went wrong while waiting");
			}
		}
		
		// Leave command adds silent +1!
		System.out.println("In: " + amountIn + " out: " + amountOut);
		
		try {
			srs.join();
		} catch (InterruptedException e) {
			System.out.println("Oops.");
		}
		
		System.out.println("Done. Bye!");
	}
}
