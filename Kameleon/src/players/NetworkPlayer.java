package players;

public class NetworkPlayer extends Player {

	private boolean checkYou = false;
	
	public NetworkPlayer(Colour inputColour, String inputName, boolean inputYou) {
		super(inputColour, inputName);
		checkYou = inputYou;
	}
	
	public boolean checkYou() {
		return checkYou;
	}
	

}