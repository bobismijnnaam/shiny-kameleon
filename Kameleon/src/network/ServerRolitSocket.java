package network;

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
		String res = "START ";
		for (int i = 0; i < Math.min(players.length, 4) - 1; i++) {
			res += players[i] + " ";
		}
		res += players[players.length - 1];
		
		sendMsg("START " + res);
	}
}
