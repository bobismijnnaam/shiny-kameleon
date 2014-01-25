package network;

public class ClientRolitSocket extends RolitSocket {
	public enum NGAMEFlags {
//		Challenge,
		Default,
		TwoPlayerGame,
		ThreePlayerGame,
		FourPlayerGame;
		
		private String s;
		
		static {
//			Challenge.s = "C";
			Default.s = "D";
			TwoPlayerGame.s = "H";
			ThreePlayerGame.s = "I";
			FourPlayerGame.s = "J";
		}
		
		public String toString() {
			return new String(s);
		}
	}
	
	public ClientRolitSocket(String addr, int port) {
		super(addr, port);
	}
	
	public void askLOGIN(String username) {
		sendMsg("LOGIN " + username);
	}
	
	public void tellVSIGN(String plain, String key) {
		String signature = PKISocket.getSignature(plain, key);
		sendMsg("VSIGN " + signature);
	}
	
	public void askSTATE() {
		sendMsg("STATE");
	}
	
	public void askNGAME(NGAMEFlags flag) {
		sendMsg("NGAME " + flag.toString());
	}
	
	public void askNGAME(String player) {
		sendMsg("NGAME C " + player);
	}
	
}
