package network;

import java.net.Socket;

public class ServerPlayer {
	public enum PlayerAuthState {
		Unathenticated,
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
	private String publickey;
	private PlayerAuthState authState;
	
	private ServerRolitSocket srs;
	private PKISocket ps;
	
	public ServerPlayer(Socket inputSocket) {
		srs = new ServerRolitSocket(inputSocket);
		authState = PlayerAuthState.Unathenticated;
	}
	
	public void start() {
		srs.start();
	}
	
	public ServerRolitSocket net() {
		return srs;
	}
	
	public PKISocket pki() {
		return ps;
	}
	
	public PlayerAuthState getAuthState() {
		return authState;
	}
	
	public void setAuthKeySent(String inputName, String inputAuth) {
		name = inputName;
		textToSign = inputAuth;
		authState = authState.next();	
		ps = new PKISocket(name);
		ps.start();
		System.out.println("Started PKI public key getter");
	}
	
	public void setAuthKeyReceived(String inputSignature) {
		signature = inputSignature;
		authState = authState.next();
	}
	
	public void tryVerifySignature() {
		if (ps.isPublicKeyReady()) {
			publickey = ps.getPublicKey();
			if (PKISocket.verifySignature(textToSign, publickey, signature)) {
				authState = authState.next();
			} else {
				authState = PlayerAuthState.Unathenticated;
			}
		}
	}
	
	public String getName() {
		return name;
	}
}
