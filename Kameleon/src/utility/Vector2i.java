package utility;

/**
 * 2 dimensional vector class, helper functions,
 * and direction type.
 * @author Bob Rubbens & Ruben Haasjes
 */
public class Vector2i implements Comparable<Vector2i> {
	
	/**
	 * Direction type including helper functions.
	 * @author Bob Rubbens
	 */
	public enum Direction {
		NorthEast,
		North,
		NorthWest,
		West,
		SouthWest,
		South,
		SouthEast,
		East;
		
		private int dirInt;
		private String dirStr; 
		
		/**
		 * For iterating over the directions with a for loop.
		 * Directions go from MIN_XXX to MAX_XXX.
		 */
		public final static int MIN_INT = 0;
		public final static int MAX_INT = 7;
		public final static Direction MIN_DIR = Direction.East;
		public final static Direction MAX_DIR = Direction.SouthEast;
		
		static {
			East.dirInt = 0;
			NorthEast.dirInt = 1;
			North.dirInt = 2;
			NorthWest.dirInt = 3;
			West.dirInt = 4;
			SouthWest.dirInt = 5;
			South.dirInt = 6;
			SouthEast.dirInt = 7;
			
			East.dirStr = "east";
			NorthEast.dirStr = "northeast";
			North.dirStr = "north";
			NorthWest.dirStr = "northwest";
			West.dirStr = "west";
			SouthWest.dirStr = "southwest";
			South.dirStr = "south";
			SouthEast.dirStr = "southeast";
		}
		
		/**
		 * @return The integer representation of each direction.
		 */
		public int toInt() {
			return dirInt;
		}
		
		/**
		 * @return String representation of a direction.
		 */
		public String toString() {
			return new String(dirStr);
		}
	}
	
	public int x, y;
	
	/**
	 * Constructs a vector of value (inputX, inputY).
	 * @param inputX - The X component of the vector.
	 * @param inputY - The Y component of the vector.
	 */
	public Vector2i(int inputX, int inputY) {
		x = inputX;
		y = inputY;
	}
	
	/**
	 * Constructs a copy of the given vector.
	 * @param other - The vector you want a copy of.
	 */
	public Vector2i(Vector2i other) {
		x = other.x;
		y = other.y;
	}
	
	/**
	 * Produces a new Vector by adding two vectors. 
	 * @param other - The vector you want to add to this one.
	 * @return A pointer to a new vector which is the addition if this vector and other.
	 */
	public Vector2i add(Vector2i other) {
		return new Vector2i(x + other.x, y + other.y);
	}
	
	/**
	 * Produces a new vector by multiplying two vectors.
	 * @param other - The vector to multiply with.
	 * @return A pointer to a new vector instance which is the product of this vector and other.
	 */
	public Vector2i product(Vector2i other) {
		return new Vector2i(x * other.x, y * other.y);
	}
	
	/**
	 * Gives the position of the direct neighbour in given direction.
	 * @param dir - The direction you want the neighbouring position of
	 * @return The neighbouring position of given direction
	 */
	public Vector2i getNeighbour(Direction dir) {
		return getNeighbour(dir.toInt());
	}
	
	/**
	 * Gives the position of the direct neighbour in given direction.
	 * @param dir - The direction you want the neighbouring position of
	 * @return The neighbouring position of given direction
	 */
	public Vector2i getNeighbour(int dir) {
		double rad = dir * Math.PI * 0.25;
		Vector2i deltaVector = new Vector2i((int) Math.round(Math.cos(rad)),
							   (int) Math.round(Math.sin(rad)));
		return add(deltaVector);
	}
	
	public boolean isNeighbour(Vector2i other) {
		int dx = Math.abs(other.x - x);
		int dy = Math.abs(other.y - y);
				// diagonal || horizontal || vertical
		return (dx == 1 && dy == 1) || (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
	}

	/**
	 * Compares by length of the vector.
	 * @return -1 if this is smaller, 0 if equal, 1 if this is bigger.
	 */
	@Override
	public int compareTo(Vector2i other) {
		int thisLength = x * x + y * y;
		int thatLength = other.x * other.x + other.y * other.y;
		if (thisLength < thatLength) {
			return -1;
		} else if (thisLength == thatLength) {
			return 0;
		} else {
			return 1;
		}
	}
	
	/**
	 * @return true if other equals to this.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (other == this) {
			return true;
		} else if (other instanceof Vector2i) {
			Vector2i otherVector2i = (Vector2i) other;
			return x == otherVector2i.x && y == otherVector2i.y;
		} else {
			return false;
		}
	}
	
	/**
	 * @return returns a string representation of the vector (x, y).
	 */
	@Override
	public String toString() {
		return "(" + Integer.toString(x) + ", " + Integer.toString(y) + ")";
	}
}
