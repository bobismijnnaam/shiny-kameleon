package network;

public enum ScoreTime {
	DAY,
	WEEK,
	MONTH;
	
	private String s;
	
	static {
		DAY.s = "DAY";
		WEEK.s = "WEEK";
		MONTH.s = "MONTH";
	}
	
	public String toString() {
		return s;
	}
}
