package leaderboard;

import java.util.Calendar;

public class Score implements Comparable<Score>, Cloneable {
	private Calendar date; // Calendar
	private String team; // Team
	private String player;
	private int score;
	
	public Score(String inputPlayer, int inputScore, String inputTeam, Calendar inputDate) {
		date = (Calendar) inputDate.clone();
		team = inputTeam == null ? null : new String(inputTeam);
		player = new String(inputPlayer);
		score = inputScore;
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
