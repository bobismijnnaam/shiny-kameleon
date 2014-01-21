package utility;

public class Vector2i implements Comparable<Vector2i> {
	public int x, y;
	
	public Vector2i(int inputX, int inputY) {
		x = inputX;
		y = inputY;
	}
	
	public Vector2i(Vector2i other) {
		x = other.x;
		y = other.y;
	}
	
	public Vector2i add(Vector2i other) {
		return new Vector2i(x + other.x, y + other.y);
	}
	
	public Vector2i product(Vector2i other) {
		return new Vector2i(x * other.x, y * other.y);
	}

	@Override
	public int compareTo(Vector2i other) {
		return x * x + y * y;
	}
	
	
}
