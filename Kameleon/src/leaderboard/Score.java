package leaderboard;

import java.util.Calendar;

public class Score implements Comparable<Score>, Cloneable {
	private Calendar date; // Calendar
	private String team; // Team
	private String player;
	private int score;
	
	public Score(String player, int score, String team, Calendar date) {
		this.date = (Calendar) date.clone();
		this.team = team == null ? null : new String(team);
		this.player = new String(player);
		this.score = score;
	}
	
	public Calendar getDate() {
		return (Calendar) date.clone();
	}
	
	public String getTag() {
		if (team != null) {
			return "[" + team + "]" + player;
		} else {
			return player;
		}
	}
	
	/**
	 * @return Returns the score.
	 */
	public int getScore() {
		return score;
	}
	
	@Override
	public int compareTo(Score o) {
		Score other = o;
		if (score < other.getScore()) {
			return -1;
		} else if (score == other.getScore()) {
			return 0;
		} else {
			return 1;
		}
	}
	
	@Override
	public Score clone() {
		return new Score(player, score, team, date);
	}
	
}
