package network;

import players.Player;
import utility.Move;
import utility.Vector2i;
import network.RolitSocket.MessageType;
import board.BoardModel;

public class ServerGame extends Thread{
	
	ServerPlayer[] players;
	String[] playerNames;
	Player[] playerObjs;
	
	BoardModel board;
	
	int turn = 0;
	private boolean running = false; 
	
	public ServerGame(ServerPlayer...inputPlayers) {
		if (inputPlayers.length <= 1) {
			throw new NullPointerException("Can't start a game with 1 or less players");
		}
		
		players = inputPlayers;
		
		playerNames = new String[players.length];
		for (int i = 0; i < players.length; i++) {
			playerNames[i] = players[i].getName();
			players[i].resetPlayerObject(i);
		}
		
		board = new BoardModel();
		
		// TODO: Make this prettier?
		playerObjs = new Player[players.length];
		playerObjs[0] = players[0].getPlayerObject();
		playerObjs[1] = players[1].getPlayerObject();
		if (players.length > 2) {
			playerObjs[2] = players[2].getPlayerObject();
		}
		if (players.length > 3) {
			playerObjs[3] = players[3].getPlayerObject();
		}
		
		switch (players.length) { // TODO: Make this prettier?
			case 2:
				board.setStartPosition(playerObjs[0], playerObjs[1]);
				break;
			case 3:
				board.setStartPosition(playerObjs[0], playerObjs[1], playerObjs[2]);
				break;
			case 4:
				board.setStartPosition(playerObjs[0], playerObjs[1], playerObjs[2], playerObjs[3]);
				break;
		}
		
	}
	
	public ServerPlayer[] getPlayers() {
		return players;
	}
	
	public void distributeTurn(int t) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				players[i].net().tellGTURN(t);
			}
		}
	}
	
	public void distributeMove(int p, Move move) {
		distributeMove(p, move.getPosition().x, move.getPosition().y);
	}
	
	public void distributeMove(int p, int x, int y) {
		for (int i = 0; i < players.length; i++) {
			if (i != p && players[i] != null) {
				players[i].net().tellGMOVE(p, x, y);
			}
		}
	}
	
	public void distributeGameEnd() {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				players[i].net().tellSTATE(PlayerState.STOPPED);
			}
		}
	}
	
	public void nextTurn() {
		turn = (turn + 1) % players.length;
		if (players[turn] == null) { // Skips players which have leaved
			turn = (turn + 1) % players.length;
		}
		
		distributeTurn(turn);
	}
	
	public void handlePlayerComms(int i, ServerPlayer p) {
		while (p.net().isNewMsgQueued()) {
			MessageType msgType = p.net().getQueuedMsgType();
			String[] msg = p.net().getQueuedMsgArray();
			switch (msgType) {
				case IG_GMOVE:
					if (turn == i) {
						Vector2i pos = new Vector2i(Integer.parseInt(msg[0]),
								Integer.parseInt(msg[1]));
						Move move = new Move(pos, playerObjs[i]);
						if (board.isMoveAllowed(move)) {
							// Move is allowed! Let's move on
							board.applyMove(move);
							distributeMove(i, move);
							
							if (board.hasWinner()) {
								distributeGameEnd();
								running = false;
							} else {
								nextTurn();
							}
						} else {
							// Move is not allowed! Send error
							p.net().tellERROR(RolitSocket.Error.InvalidLocationException,
									"Bad carrion (invalid move)");
						}
					} else {
						p.net().tellWARNI("Honeybadger says you are not "
								+ "supposed do a move!"
								+ "Be patient and wait for your turn or "
								+ "Honeybadger will eat you!");
					}
					break;
				case AL_LEAVE:
					p.net().close();
					players[i] = null;
				default:
					p.net().tellERROR(RolitSocket.Error.UnexpectedOperationException,
							msgType.toString());
					break;
			}
		}
	}
	
	public void run() {
		for (ServerPlayer p : players) {
			p.net().tellSTART(playerNames);
		}
		
		running = true;
		
		while (running) {
			for (int i = 0; i < players.length; i++) {
				ServerPlayer p = players[i];
				if (p != null) {
					handlePlayerComms(i, p);
				}
			}
			
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
