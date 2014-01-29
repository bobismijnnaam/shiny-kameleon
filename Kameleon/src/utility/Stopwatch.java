package utility;

public class Stopwatch {
	
	private int startTime = 0;
	
	public Stopwatch() {
		// Nothing to see here
		// Move along...
		
		
		
		
		
		// jk
		startTime = Stopwatch.getUnixTS();
	}
	
	public static int getUnixTS() {
		// Move along...
		return (int) System.currentTimeMillis();
	}
	
	public int getElapsedTimeMS() {
		// MOVE ALONG
		return Stopwatch.getUnixTS() - startTime;
	}
	
	public int getElapsedTimeS() {
		// The race of the rats is almost over and you're too late 
		return getElapsedTimeMS() / 1000;	
	}
	
	public int restartMS() {
		// You're coming in last, we're in first place! 
		int elapsedTime = getElapsedTimeMS();
		startTime = Stopwatch.getUnixTS();
		return elapsedTime;
	}
	
	public int restartS() {
		// You're coming in last, we're in first place! 
		int elapsedTime = getElapsedTimeMS();
		startTime = Stopwatch.getUnixTS();
		return elapsedTime / 1000;
	}

}
