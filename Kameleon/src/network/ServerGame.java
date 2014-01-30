package network;

import players.Player;
import utility.Move;
import utility.Vector2i;
import network.RolitSocket.MessageType;
import board.BoardModel;

public class ServerGame extends Thread {
	
	ServerPlayer[] players;
	String[] playerNames;
	Player[] playerObjs;
	
	BoardModel board;
	
	int turn = 0;
	private boolean running = false; 
	private boolean finished = false;
	
	/**
	 * Constructs a ServerGame object which controls the gameflow
	 * and sends appropriate messages to the clients
	 * @param inputPlayers - The ServerPlayers who will be
	 * participating in the game.
	 */
	public ServerGame(ServerPlayer...inputPlayers) {
		if (inputPlayers.length <= 1) {
			throw new NullPointerException("Can't start a game with 1 or less players");
		}
		
		players = inputPlayers;
		
		playerNames = new String[players.length];
		playerObjs = new Player[players.length];
		for (int i = 0; i < players.length; i++) {
			playerNames[i] = players[i].getName();
			players[i].resetPlayerObject(i + 1);
			playerObjs[i] = players[i].getPlayerObject();
		}
		
		board = new BoardModel();
		
		switch (players.length) {
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
	
	/**
	 * Prints an arbitary message to the console
	 * with a prefix [Game].
	 * @param msg - The message
	 */
	public void gameSays(String msg) {
		System.out.println("\t[Game] " + msg);
	}
	
	/**
	 * Checks whether or not the player is in the game.
	 * @param player - The name of the player to look for
	 * @return
	 */
	public boolean isPlayerInGame(String player) {
		for (String s : playerNames) {
			if (s.equals(player)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * To check if the game is finished.
	 * @return True if the game is finished, otherwise false
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * Returns a ServerPlayer[] of players in this game.
	 * @return The ServerPlayer[]
	 */
	public ServerPlayer[] getPlayers() {
		return players;
	}
	
	/**
	 * Tells whose turn it is to all participating players.
	 * The function makes the turn compliant with the protocol
	 * (Protocol dictates [1-4], server uses [0-3]
	 * @param t The turn [0-3].
	 */
	public void distributeTurn(int t) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				players[i].net().tellGTURN(t + 1);
				System.out.println(t + 1);
			}
		}
	}
	
	/**
	 * Tells the move by a certain player to all participating players.
	 * @param p - The player who does the move
	 * @param move - The move itself
	 */
	public void distributeMove(int p, Move move) {
		distributeMove(p, move.getPosition().x, move.getPosition().y);
	}
	
	/**
	 * Tells the move by a certain player to all participating players.
	 * @param p - The player who does the move
	 * @param x - The X coordinate
	 * @param y - The Y coordinate
	 */
	public void distributeMove(int p, int x, int y) {
		for (int i = 0; i < players.length; i++) {
			players[i].net().tellGMOVE(p + 1, x, y);
		}
	}
	
	/**
	 * Notifies all the players that the game has ended.
	 */
	public void distributeGameEnd() {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				players[i].net().tellSTATE(PlayerState.STOPPED);
			}
		}
	}
	
	/**
	 * Calculates who is next, and tells all the clients about it.
	 */
	public void nextTurn() {
		turn = (turn + 1) % players.length;
		if (players[turn] == null) { // Skips players which have leaved
			turn = (turn + 1) % players.length;
		}
		
		distributeTurn(turn);
	}
	
	/**
	 * Handles the player mechanics.
	 * @param i - The place of this player in the order of players
	 * @param p - The ServerPlayer instance
	 */
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
							
							gameSays(players[i].getName() + " made a move: " + pos.toString());
							
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
					break;
				case AL_STATE:
					p.net().tellSTATE(PlayerState.PLAYING);
					break;
				default:
					p.net().tellERROR(RolitSocket.Error.UnexpectedOperationException,
							msgType.toString());
					break;
			}
		}
	}
	
	/**
	 * The ServerGame main loop.
	 */
	public void run() {
		for (ServerPlayer p : players) {
			p.net().tellSTART(playerNames);
			p.net().tellGTURN(turn + 1);
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
		
		finished = true;
	}
}
