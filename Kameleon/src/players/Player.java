package players;

public class Player {
	
	/**
	 * 
	 * @author Got the pattern from Pshemo
	 * http://stackoverflow.com/questions/18883646/java-enum-methods
	 *
	 */
	public enum Colour {
		Blue,
		Green,
		Red,
		Yellow;
		
		private String clr;
		private String clrNormalised;
		private Colour next;
		
		static {
			Blue.clrNormalised = "Blue  ";
			Green.clrNormalised = "Green ";
			Red.clrNormalised = "Red   ";
			Yellow.clrNormalised = "Yellow";
			
			Blue.clr = "Blue";
			Green.clr = "Green";
			Red.clr = "Red";
			Yellow.clr = "Yellow";
			
			Red.next = Yellow;
			Yellow.next = Green;
			Green.next = Blue;
			Blue.next = Red;
		}
		
		public String toString() {
			return new String(clr);
		}
		
		public String toNormalisedString() {
			return new String(clrNormalised);
		}
		
		public Colour getNext() {
			return next;
		}
		
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
	
	public static void main(String[] args) {
		Colour c1 = Colour.Green;
		System.out.println(c1.toString());
	}
	
}
