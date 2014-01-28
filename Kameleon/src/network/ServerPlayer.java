package network;

import java.net.Socket;

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
//	private String publickey;
	private PlayerAuthState authState;
	
//	private boolean defaultSupport;
	private boolean chatSupport;
	private boolean lobbySupport;
	
	private ServerRolitSocket srs;
//	private PKISocket ps;
	
	public ServerPlayer(Socket inputSocket) {
		srs = new ServerRolitSocket(inputSocket);
		authState = PlayerAuthState.Unauthenticated;
		
//		defaultSupport = false;
		chatSupport = false;
		lobbySupport = false;
	}
	
	public void start() {
		srs.start();
	}
	
	public ServerRolitSocket net() {
		return srs;
	}
	
//	public PKISocket pki() {
//		return ps;
//	}
	
	public PlayerAuthState getAuthState() {
		return authState;
	}
	
	public void setAuthKeySent(String inputName, String inputAuth) {
		name = inputName;
		textToSign = inputAuth;
		authState = authState.next();	
//		ps = new PKISocket(name);
//		ps.start();
		
		net().askVSIGN(textToSign);
	}
	
	public void setAuthKeyReceived(String inputSignature) {
		signature = inputSignature;
		authState = authState.next();
	}
	
	public PlayerAuthState tryVerifySignature(String publickey) {
//		if (ps.isPublicKeyReady()) {
//			publickey = ps.getPublicKey();
		if (PKISocket.verifySignature(textToSign, publickey, signature)) {
			authState = authState.next();
		} else {
			authState = PlayerAuthState.Unauthenticated;
		}
//		}
		
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
