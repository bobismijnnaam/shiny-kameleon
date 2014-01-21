package leaderboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Leaderboard {
	ArrayList<Score> scores;
	
	public Leaderboard() {
		scores = new ArrayList<Score>(0);
	}
	
	public void addScore(String player, int score, String team, Calendar date) {
		if (scores.size() == 0) {
			scores.add(new Score(player, score, team, date));
		} else {
			for (int i = 0; i < scores.size(); i++) {
				if (scores.get(i).getScore() < score) {
					scores.add(i, new Score(player, score, team, date));
					break;
				}
			}
		}
	}
	
	public List<Score> getScores() {
		ArrayList<Score> newList = new ArrayList<Score>(scores.size());
		for (Score s : scores) {
			newList.add(s.clone());
		}
		return newList;
	}
	
	public List<Score> getTopScores(int n) {
		int size = Math.min(n, scores.size());
		ArrayList<Score> newList = new ArrayList<Score>(size);
		for (int i = 0; i < size; i++) {
			newList.add(scores.get(i));
		}
		return newList;
	}
	
	public List<Score> getScoresAbove(int n) {
		int limit = scores.size();
		for (int i = 0; i < scores.size(); i++) {
			if (scores.get(i).getScore() < n) {
				limit = i;
				break;
			}
		}
		
		if (limit == 0) {
			return null;
		} else {
			ArrayList<Score> newList = new ArrayList<Score>(limit);
			for (int i = 0; i < limit; i++) {
				newList.add(scores.get(i));
			}
			return newList;
		}
	}
	
	public List<Score> getScoresBelow(int n) {
		int start = -1;
		for (int i = 0; i < scores.size(); i++) {
			if (scores.get(i).getScore() < n) {
				start = i;
				break;
			}
		}
		
		if (start == -1) {
			return null;
		} else {
			ArrayList<Score> newList = new ArrayList<Score>(scores.size() - start);
			for (int i = start; i < scores.size(); i++) {
				newList.add(scores.get(i));
			}
			return newList;
		}
	}
	
	public static void main(String[] args) {
		Leaderboard lb = new Leaderboard();
		Calendar date = Calendar.getInstance();
		
		date.set(2013,  12, 23, 12, 30);
		lb.addScore("Bob", 10, "De Bobbels", date);
		
		date.set(2013, 12, 23, 12, 31);
		lb.addScore("Ruben", 11, "De Rubens", date);
		
		date.set(2013,  12, 24, 12, 35);
		lb.addScore("Dennis", 90, null, date);
		
		date.set(2013,  12, 23, 12, 30);
		lb.addScore("Bob", 42, "De Bobbels", date);
		
		date.set(2013, 12, 23, 12, 31);
		lb.addScore("Ruben", 18, "De Rubens", date);
		
		date.set(2013,  12, 24, 12, 35);
		lb.addScore("Dennis", 75, null, date);
		
		List<Score> result;
		result = lb.getScoresBelow(11);
		for (Score s : result) {
			System.out.println(s.getScore());
		}
	}
}
