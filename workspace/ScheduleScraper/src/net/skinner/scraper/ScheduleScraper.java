package net.skinner.scraper;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScheduleScraper {
	public static void main(String[] args) {
		try {
			ScheduleScraper.run();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void run() throws SQLException {
		// Test the database connection before doing anything
		java.sql.Connection conn = getConnection();
		ArrayList<Game> games;
		
		boolean areGames = areGames(conn);
		conn.close();
		
		if(areGames){
			Calendar c = Calendar.getInstance();
			int month = c.get(Calendar.MONTH)+1;
			games = scrape(month,12);
			insert(games);
		} else {
			games = scrape(3,12);
			update(games);
		}
		
		if(games == null){
			return;
		}

	}
	
	private static java.sql.Connection getConnection() throws SQLException {
		java.sql.Connection conn = DriverManager.getConnection("jdbc:mysql://askinner.net:3306/skin452_wtw","skin452_admin","xxx");
		System.out.println("Connection to database successful!");
		return conn;
	}
	
	private static void update(ArrayList<Game> games) throws SQLException {
		java.sql.Connection conn = getConnection();
		
		PreparedStatement statement = null;
		ResultSet rs;
		
		statement = conn.prepareStatement("SELECT Game.id, home.name, away.name, homeScore, awayScore, date FROM Game "
				+ "JOIN Team home ON home_id = home.id "
				+ "JOIN Team away ON away_id = away.id "
				+ "ORDER BY date ASC");
		rs = statement.executeQuery();
		ArrayList<Game> leftOverGames = new ArrayList<Game>();
		
		while(rs.next()){
			int id = rs.getInt("Game.id");
			String homeTeam = rs.getString("home.name");
			String awayTeam = rs.getString("away.name");
			int homeScore = rs.getInt("homeScore");
			int awayScore = rs.getInt("awayScore");
			Long dateLong = rs.getLong("date");
			
			boolean match = false;
			Game gameToRemove = null;
			for (Game game : games) {
				if(game.getHomeTeam().equals(homeTeam) && game.getAwayTeam().equals(awayTeam) && game.getDate().getTime() == dateLong){
					match = true;
					gameToRemove = game;
					break;
				}
			}
			
			if(match){
				games.remove(gameToRemove);
			} else {
				leftOverGames.add(new Game(homeTeam, awayTeam, homeScore, awayScore, id));
			}
		}
		
		// Update ALL games
		for (Game game : games) {
			Game gameToRemove = null;
			for (Game leftOverGame : leftOverGames) {
				if(game.getHomeTeam().equals(leftOverGame.getHomeTeam()) && game.getAwayTeam().equals(leftOverGame.getAwayTeam())){
					gameToRemove = leftOverGame;
					break;
				}
			}
			
			if(gameToRemove == null){
				// Do a standard update
				int id = getGameID(game);
			} else {
				int id = gameToRemove.getId();
				System.out.println("---------");
				System.out.println("Replacing");
				System.out.println(gameToRemove);
				System.out.println("With");
				System.out.println(game);
				System.out.println("ID: " + id);
				
				leftOverGames.remove(gameToRemove);
				
				// Update the game in the database
				updateGame(conn, id, game);
				
				System.out.println("Updated " + statement.getUpdateCount() + " rows");
				System.out.println("---------");
			}
		}
			
		conn.close();
	}

	private static int getGameID(Game game) {
		return 0;
	}

	private static void updateGame(java.sql.Connection conn, int id, Game game) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("UPDATE Game SET date=?, home_score=?, away_score=?, tv=?, week=? "
				+ "WHERE id=?");
		statement.setLong(1, game.getDate().getTime());
		if(game.getHomeScore() == null){
			statement.setNull(2, Types.INTEGER);
			statement.setNull(3, Types.INTEGER);
		} else {
			statement.setInt(2, game.getHomeScore());
			statement.setInt(3, game.getAwayScore());
		}
		statement.setString(4, game.getChannels());
		statement.setInt(5, game.getWeek());
		statement.setInt(6, id);
		statement.executeUpdate();
	}

	private static void insert(ArrayList<Game> games) {
		// TODO Auto-generated method stub
		
	}

	private static boolean areGames(java.sql.Connection conn) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM Game");
		ResultSet rs = statement.executeQuery();
		if(rs.next()){
			return true;
		}
		return false;
	}
	
	private static ArrayList<Game> scrape(int startMonth, int endMonth) {
		ArrayList<Game> games = new ArrayList<Game>();
		String baseURL = "http://www.mlssoccer.com/schedule";
		
		int week = 0;
		Date endWeek = null;
		DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mma zzz");
		for (int month = startMonth; month <= endMonth; month++) {
			System.out.println("Scraping month " + month);
			Connection Jconn = Jsoup.connect(baseURL + "?month=" + month);
			
			Document scheduleDoc = null;
			int retries = 0;
			while(scheduleDoc == null){
				try {
					scheduleDoc = Jconn.get();
				} catch (IOException e) {
					if(retries >= 10){
						e.printStackTrace();
						return null;
					}
					System.out.println("Failed to get document, trying again");
					retries++;
				}
			}
			
			Elements scheduleSection = scheduleDoc.select("div.schedule-page");
			Elements dates = scheduleSection.select("h3");
			Elements scheduleTables = scheduleSection.select("table");
			
			for (int i = 0; i < scheduleTables.size(); i++) {
				String dateString = dates.get(i).text();
				Elements gameRows = scheduleTables.get(i).select("tbody").select("tr");
				for (Element gameRow : gameRows) {
					String competition = gameRow.select("span.competetion").text();
					if(competition.contains("MLS")){
						boolean playoffs = false;
						if(competition.contains("Playoffs") || competition.contains("Cup")){
							playoffs = true;
						}
						
						String time = gameRow.select("div.field-game-date-start-time").text();		
						if(time.equals("TBD")){
							time = "12:00PM EST";
						}
						String stadium = gameRow.select("div.field-competition-venue").text().replace(competition, "").replace("at ", "").trim();
						String homeTeam = gameRow.select("div.field-home-team").text();
						String awayTeam = gameRow.select("div.field-away-team").text();
						String score = gameRow.select("div.field-score").text().replace(" ", "");
						String[] scoreSplit = score.split("-");
						Integer homeScore = null;
						Integer awayScore = null;
						try{
							homeScore = Integer.parseInt(scoreSplit[0]);
							awayScore = Integer.parseInt(scoreSplit[1]);
						} catch(Exception e) {
							
						}
						
						String channels = "";
						Elements channelElements = gameRow.select("div.field-broadcast-partners").select("strong");
						for (int j = 0; j < channelElements.size(); j++) {
							String channel = channelElements.get(j).text();
							
							channels += channel;
							if(j < channelElements.size()-1){
								channels += ";";
							}
						}
						if(channels.equals("")){
							channels = "MLS LIVE";
						}
						
						Date date = null;
						try {
							date = dateFormat.parse(dateString + " " + time);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						
						if(endWeek==null){
							Calendar c = Calendar.getInstance();
							c.setTime(date);
							c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
							c.set(Calendar.HOUR, 0);
							c.set(Calendar.MINUTE, 0);
							c.set(Calendar.SECOND, 0);
							c.set(Calendar.MILLISECOND, 0);
							c.add(Calendar.WEEK_OF_YEAR, 1);
							endWeek = c.getTime();
						} else {
							if(date.after(endWeek)){
								while(date.after(endWeek)){
									Calendar c = Calendar.getInstance();
									c.setTime(endWeek);
									c.add(Calendar.WEEK_OF_YEAR, 1);
									endWeek = c.getTime();
								}
								week++;
							}
						}
						
						games.add(new Game(homeTeam, awayTeam, homeScore, awayScore, date, week, stadium, channels, playoffs));
					}
				}
			}
				
		}
		
		return games;
	}
	

}
