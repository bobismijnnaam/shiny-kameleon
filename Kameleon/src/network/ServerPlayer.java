package network;

import java.net.Socket;

import players.Player;

public class ServerPlayer {
	public enum PlayerAuthState {
		Unauthenticated,
		KeySent,
		SignatureAwaitsChecking,
		Authenticated;
		
		public PlayerAuthState next() {
			return values()[Math.min(ordinal() + 1, values().length - 1)];
		}
	}
	
	private String name;
	private String textToSign;
	private String signature;
	private PlayerAuthState authState;
	
	private boolean chatSupport = false;
	private boolean lobbySupport = false;
	
	private ServerRolitSocket srs;
	
	private Player playerObj = null;
	
	public ServerPlayer(Socket inputSocket) {
		srs = new ServerRolitSocket(inputSocket);
		authState = PlayerAuthState.Unauthenticated;
	}
	
	public void start() {
		srs.start();
	}
	
	public ServerRolitSocket net() {
		return srs;
	}
	
	public void resetPlayerObject(int i) {
		switch (i) {
			case 0:
				playerObj = null;
				break;
			case 1:
				playerObj = new Player(Player.Colour.Red, getName());
				break;
			case 2:
				playerObj = new Player(Player.Colour.Yellow, getName());
				break;
			case 3:
				playerObj = new Player(Player.Colour.Green, getName());
				break;
			case 4:
				playerObj = new Player(Player.Colour.Blue, getName());
				break;
		}
	}
	
	public Player getPlayerObject() {
		return playerObj;
	}
	
	public PlayerAuthState getAuthState() {
		return authState;
	}
	
	public void setAuthKeySent(String inputName, String inputAuth) {
		setName(inputName);
		
		textToSign = inputAuth;
		authState = authState.next();	
		
		net().askVSIGN(textToSign);
	}
	
	public void setName(String inputName) {
		name = inputName;
	}
	
	public void setAuthKeyReceived(String inputSignature) {
		signature = inputSignature;
		authState = authState.next();
	}
	
	public PlayerAuthState tryVerifySignature(String publickey) {
		if (PKISocket.verifySignature(textToSign, publickey, signature)) {
			authState = authState.next();
		} else {
			authState = PlayerAuthState.Unauthenticated;
		}
		
		return authState;
	}
	
	public void setClientType(String flags) {
		if (flags.contains("C")) {
			chatSupport = true;
		}
		if (flags.contains("L")) {
			lobbySupport = true;
		}
	}
	
	public boolean isChatSupported() {
		return chatSupport;
	}
	
	public boolean isLobbySupported() {
		return lobbySupport;
	}
	
	public String getName() {
		return name;
	}
}
