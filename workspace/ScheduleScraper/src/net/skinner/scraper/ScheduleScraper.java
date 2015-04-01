package net.skinner.scraper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScheduleScraper {
	public static void main(String[] args) {
		try {
			final Counter counter = new Counter();
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(new Runnable() {
				
				public void run() {
					try {
						if(counter.getCount() == 0){
							fullScrape();
						} else {
							scoreScrape();
							
							if(counter.getCount() == 12){
								counter.set(0);
							}
						}
						counter.add();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 0, 1, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void fullScrape() throws SQLException, IOException {
		ArrayList<Game> games;
		
		// If there are games in the db, update them, or else insert them
		if(areGames()){
			games = scrape(3,12);
			update(games);
		} else {
			games = scrape(3,12);
			insert(games);
		}
	}

	private static void scoreScrape() throws SQLException, IOException {
		int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
		ArrayList<Game> games = scrape(m,m);
		updateScores(games);
	}
	
	private static java.sql.Connection getConnection() throws SQLException, IOException {
		Properties props = new Properties();
		String propName = "dbInfo.properties";
		InputStream in = new FileInputStream(propName);
		props.load(in);
		in.close();
		
		java.sql.Connection conn = DriverManager.getConnection(props.getProperty("url"),props.getProperty("user"),
				props.getProperty("password"));
		System.out.println("Connection to database successful!");
		return conn;
	}
	
	private static void updateScores(ArrayList<Game> games) throws SQLException, IOException {
		java.sql.Connection conn = getConnection();
		PreparedStatement statement = conn.prepareStatement("SELECT Game.id, home.name, away.name, date, home_score FROM Game "
				+ "JOIN Team home ON home_id = home.id "
				+ "JOIN Team away ON away_id = away.id "
				+ "ORDER BY date ASC");
		ResultSet rs = statement.executeQuery();
		while(rs.next()){
			int id = rs.getInt("Game.id");
			String homeTeam = rs.getString("home.name");
			String awayTeam = rs.getString("away.name");
			Long dateLong = rs.getLong("date");
			
			rs.getInt("home_score");
			// if the score is not null check if there is a new score
			if(rs.wasNull()){
				Game gameToRemove = null;
				for (Game game : games) {
					if(game.getHomeScore() != null){
						if(game.getHomeTeam().equals(homeTeam) && game.getAwayTeam().equals(awayTeam)
									&& game.getDate().getTime() == dateLong){
							gameToRemove = game;
							statement = conn.prepareStatement("UPDATE Game SET home_score=?, away_score=? WHERE id=?");
							statement.setInt(1, game.getHomeScore());
							statement.setInt(2, game.getAwayScore());
							statement.setInt(3, id);
							statement.executeUpdate();
							System.out.println("Updated score for " + game);
							break;
						}
					}
				}
				if(gameToRemove != null){
					games.remove(gameToRemove);
				}
			}
		}
	}
	
	private static void update(ArrayList<Game> games) throws SQLException, IOException {
		java.sql.Connection conn = getConnection();
		
		PreparedStatement statement = null;
		ResultSet rs;
		
		statement = conn.prepareStatement("SELECT Game.id, home.name, away.name, date FROM Game "
				+ "JOIN Team home ON home_id = home.id "
				+ "JOIN Team away ON away_id = away.id "
				+ "ORDER BY date ASC");
		rs = statement.executeQuery();
		ArrayList<Game> leftOverGames = new ArrayList<Game>();
		
		while(rs.next()){
			int id = rs.getInt("Game.id");
			String homeTeam = rs.getString("home.name");
			String awayTeam = rs.getString("away.name");
			Long dateLong = rs.getLong("date");
			
			boolean match = false;
			Game matchedGame = null;
			for (Game game : games) {
				if(game.getHomeTeam().equals(homeTeam) && game.getAwayTeam().equals(awayTeam) && game.getDate().getTime() == dateLong){
					match = true;
					matchedGame = game;
					break;
				}
			}
			
			if(match){
				// Do a standard update
				updateGame(conn, id, matchedGame);
				games.remove(matchedGame);
			} else {
				// Add to left over pile to check later
				leftOverGames.add(new Game(homeTeam, awayTeam, id));
			}
		}
		
		// Update database with changed match dates
		for (Game game : games) {
			Game gameToRemove = null;
			for (Game leftOverGame : leftOverGames) {
				if(game.getHomeTeam().equals(leftOverGame.getHomeTeam()) && game.getAwayTeam().equals(leftOverGame.getAwayTeam())){
					gameToRemove = leftOverGame;
					break;
				}
			}
			
			if(gameToRemove == null){
				// This is a new game, insert into db
				insertGame(conn, game);
			} else {
				int id = gameToRemove.getId();
				leftOverGames.remove(gameToRemove);
				
				System.out.println("---------");
				System.out.println("Changed date");
				
				// Update the game in the database
				updateGame(conn, id, game);
				
				System.out.println("---------");
			}
		}
			
		conn.close();
	}

	private static void insertGame(java.sql.Connection conn, Game game) throws SQLException {
		System.out.println("Inserting " + game + "\n");
		
		// Get home team ID
		int homeID = getTeamID(conn, game.getHomeTeam());
		int awayID = getTeamID(conn, game.getAwayTeam());
		
		PreparedStatement statement = conn.prepareStatement("INSERT INTO Game (home_id, away_id, date, week, home_score, away_score, "
				+ "tv, stadium, playoffs) "
				+ "VALUES (?,?,?,?,?,?,?,?,?)");
		statement.setInt(1, homeID);
		statement.setInt(2, awayID);
		statement.setLong(3, game.getDate().getTime());
		statement.setInt(4, game.getWeek());
		if(game.getHomeScore() == null){
			statement.setNull(5, Types.INTEGER);
			statement.setNull(6, Types.INTEGER);
		} else {
			statement.setInt(5, game.getHomeScore());
			statement.setInt(6, game.getAwayScore());
		}
		statement.setString(7, game.getChannels());
		statement.setString(8, game.getStadium());
		statement.setBoolean(9, game.isPlayoffs());
		statement.executeUpdate();
	}

	// Gets the team ID by team name
	// If the team doesn't exist, this method will create it
	private static int getTeamID(java.sql.Connection conn, String teamName) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("SELECT id FROM Team WHERE name = ?");
		statement.setString(1, teamName);
		ResultSet rs = statement.executeQuery();
		if(rs.next()){
			return rs.getInt(1);
		} else {
			statement = conn.prepareStatement("INSERT INTO Team (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, teamName);
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			return rs.getInt(1);
		}
	}

	private static void updateGame(java.sql.Connection conn, int id, Game game) throws SQLException {
		System.out.println("Updating " + game + " at id: " + id + "\n");
		PreparedStatement statement = conn.prepareStatement("UPDATE Game SET date=?, week=?, home_score=?, away_score=?, tv=?, stadium=? "
				+ "WHERE id=?");
		statement.setLong(1, game.getDate().getTime());
		statement.setInt(2, game.getWeek());
		if(game.getHomeScore() == null){
			statement.setNull(3, Types.INTEGER);
			statement.setNull(4, Types.INTEGER);
		} else {
			statement.setInt(3, game.getHomeScore());
			statement.setInt(4, game.getAwayScore());
		}
		statement.setString(5, game.getChannels());
		statement.setString(6, game.getStadium());
		statement.setInt(7, id);
		statement.executeUpdate();
	}

	private static void insert(ArrayList<Game> games) throws SQLException, IOException {
		java.sql.Connection conn = getConnection();
		for (Game game : games) {
			insertGame(conn, game);
		}
		conn.close();
	}

	private static boolean areGames() throws SQLException, IOException {
		java.sql.Connection conn = getConnection();
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM Game");
		ResultSet rs = statement.executeQuery();
		
		boolean areGames = rs.next();
		conn.close();
		
		if(areGames){
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
						if(!competition.contains("Regular Season")){
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