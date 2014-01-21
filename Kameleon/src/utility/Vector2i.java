package utility;

public class Vector2i implements Comparable<Vector2i> {
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
	 * Compares
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
	
	
}
