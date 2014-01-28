package network;

import board.BoardModel;

public class ServerGame extends Thread{
	
	ServerPlayer[] players;
	String[] playerNames;
	
	BoardModel board;
	
	int turn = 0;
	
	public ServerGame(ServerPlayer...inputPlayers) {
		players = inputPlayers;
		
		playerNames = new String[players.length];
		for (int i = 0; i < players.length; i++) {
			playerNames[i] = players[i].getName();
			players[i].resetPlayerObject(i);
		}
		
		board = new BoardModel();
	}
	
	public ServerPlayer[] getPlayers() {
		return players;
	}
	
	public void run() {
		for (ServerPlayer p : players) {
			p.net().tellSTART(playerNames);
		}
		
		
	}
}
