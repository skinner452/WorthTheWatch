package net.skinner.scraper;

import java.util.Date;

public class Game {
	private boolean playoffs;
	private String stadium;
	private String channels;
	private String homeTeam;
	private String awayTeam;
	private Integer homeScore;
	private Integer awayScore;
	private Date date;
	private int week;
	private int id;
	
	public Game(String homeTeam, String awayTeam, Integer homeScore, Integer awayScore, Date date,
			int week, String stadium, String channels, boolean playoffs) {
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.homeScore = homeScore;
		this.awayScore = awayScore;
		this.date = date;
		this.week = week;
		this.stadium = stadium;
		this.channels = channels;
		this.playoffs = playoffs;
	}
	
	public boolean isPlayoffs() {
		return playoffs;
	}

	public String getStadium() {
		return stadium;
	}

	public String getChannels() {
		return channels;
	}

	public String getHomeTeam() {
		return homeTeam;
	}

	public String getAwayTeam() {
		return awayTeam;
	}

	public Integer getHomeScore() {
		return homeScore;
	}

	public Integer getAwayScore() {
		return awayScore;
	}

	public Date getDate() {
		return date;
	}

	public int getWeek() {
		return week;
	}

	public int getId() {
		return id;
	}

	// Database game
	public Game(String homeTeam, String awayTeam, int id){
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.id = id;
	}
	
	@Override
	public String toString() {
		return date + ": " + homeTeam + " - " + awayTeam + " (" + homeScore + " - " + awayScore + ")";
	}

	
}
