package network;

public enum PlayerState {
	WAITING,
	UNKNOWN,
	LOBBY,
	STARTING,
	PLAYING,
	STOPPED;
	
	private String s;
	
	static {
		WAITING.s = "WAITING";
		UNKNOWN.s = "UNKNOWN";
		LOBBY.s = "LOBBY";
		STARTING.s = "STARTING";
		PLAYING.s = "PLAYING";
		STOPPED.s = "STOPPED";
	}
	
	public String toString() {
		return new String(s);
	}
}
