package logic;

public class Player {
	
	enum Colour {
		Blue,
		Green,
		Red,
		Yellow
	}
	
	private Colour colour;
	private String name;

	/**
	 * @param inputColour - The desired colour in game;
	 * @param inputName - The desired name in game;
	 */
	public Player(Colour inputColour, String inputName) {
		colour = inputColour;
		name = inputName;
	}

	/**
	 * @return name - A deepcopy of the name used in game;
	 */
	public String getName() {
		return new String(name);
	}
	
	/**
	 * @return colour - The colour used in game;
	 */
	public Colour getColour() {
		return colour;
	}
	
}
