package leaderboard;

import java.util.Calendar;

import players.Player;

public class Score implements Comparable<Score>, Cloneable {
	private Calendar date; // Calendar
	private String team; // Team
	private Player player;
	private int score;
	
	public Score(String inputPlayer, int inputScore, String inputTeam, Calendar inputDate) {
		date = (Calendar) inputDate.clone();
		team = inputTeam == null ? null : new String(inputTeam);
		player = new Player(Player.Colour.Blue , inputPlayer);
		score = inputScore;
	}
	
	public Calendar getDate() {
		return (Calendar) date.clone();
	}
	
	public String getTag() {
		if (team != null) {
			return "[" + team + "]" + player.getName();
		} else {
			return player.getName();
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
		return new Score(player.getName(), score, team, date);
	}
	
}
