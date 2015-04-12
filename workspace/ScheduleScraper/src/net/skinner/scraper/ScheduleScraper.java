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
			final ScheduleScraper s = new ScheduleScraper();
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(new Runnable() {
				
				public void run() {
					s.scrape();
				}
			}, 0, 1, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int nextUpdate; // in minutes
	private int nextFullScrape; // in minutes
	private ArrayList<Game> games;
	private boolean notFirstRun;
	
	public void scrape(){
		boolean scraped = true;
		
		if(nextUpdate <= 0 && notFirstRun){
			System.out.println();
			System.out.println("-----------------");
			System.out.println("Score scrape started at: " + new Date());
			try {
				scoreScrape();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Finished at: " + new Date());
			System.out.println("-----------------");
			
			nextFullScrape--;
		} else if(nextFullScrape <= 0){
			System.out.println();
			System.out.println("-----------------");
			System.out.println("Full scrape started at: " + new Date());
			try {
				fullScrape();
				nextFullScrape = 60*24; // 1 day
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Finished at: " + new Date());
			System.out.println("-----------------");
			
			nextUpdate--;
		} else {
			nextUpdate--;
			nextFullScrape--;
			scraped = false;
		}
		
		if(!notFirstRun){
			notFirstRun = true;
		}
		
		if(scraped){
			// Find next time to update score
			Calendar currentTime = Calendar.getInstance();
			
			
			// This should set the update time to 1 min if there is a game
			for (Game game : games) {
				if(!game.hasScore()){
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(game.getDate());
					endTime.add(Calendar.HOUR, 1);
					endTime.add(Calendar.MINUTE, 50);
					
					if(endTime.before(currentTime)){
						System.out.println("Waiting for score of " + game);
						nextUpdate = 1;
						return;
					}
				}
			}
			
			// Calculate time until next game end
			Date nextGame = null;
			for (Game game : games) {
				if(!game.hasScore()){
					if(nextGame == null || game.getDate().before(nextGame)){
						nextGame = game.getDate();
					}
				}
			}
			
			currentTime = Calendar.getInstance();
			Calendar nextGameTime = Calendar.getInstance();
			nextGameTime.setTime(nextGame);
			nextGameTime.add(Calendar.HOUR, 1);
			nextGameTime.add(Calendar.MINUTE, 50);
			long nextUpdateInMili = nextGameTime.getTimeInMillis() - currentTime.getTimeInMillis();
			nextUpdate = (int)(nextUpdateInMili / 60000);
			
			System.out.println("Next update in " + nextUpdate + " minutes");
		} else {
			if(nextUpdate%60==0){
				System.out.println("Next update: " + nextUpdate/60 + " hours");
			}

			if(nextFullScrape%60==0){
				System.out.println("Next full scrape: " + nextFullScrape/60 + " hours");
			}
		}
	}
	
	private ArrayList<Game> createCopy(){
		ArrayList<Game> gamesCopy = new ArrayList<Game>();
		for (Game game : games) {
			gamesCopy.add(game);
		}
		return gamesCopy;
	}
	
	private void fullScrape() throws SQLException, IOException {
		scrape(3,12);
		
		ArrayList<Game> gamesCopy = createCopy();
		
		System.out.println("Games size: " + games.size());
		// If there are games in the db, update them, or else insert them
		if(areGames()){
			update();
		} else {
			insert();
		}
		
		games = gamesCopy;
	}

	private void scoreScrape() throws SQLException, IOException {
		int m = Calendar.getInstance().get(Calendar.MONTH) + 1;
		scrape(m,m);
		
		ArrayList<Game> gamesCopy = createCopy();
		updateScores();
		games = gamesCopy;
	}
	
	private java.sql.Connection getConnection() throws SQLException, IOException {
		Properties props = new Properties();
		String propName = "dbInfo.properties";
		InputStream in = new FileInputStream(propName);
		props.load(in);
		in.close();
		
		java.sql.Connection conn = DriverManager.getConnection(props.getProperty("url"),props.getProperty("user"),
				props.getProperty("password"));
		System.out.println("Connected to database");
		return conn;
	}
	
	private void updateScores() throws SQLException, IOException {
		System.out.println("Updating scores");
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
		conn.close();
	}
	
	private void update() throws SQLException, IOException {
		System.out.println("Performing update for " + games.size() + " games");
		java.sql.Connection conn = getConnection();
		
		PreparedStatement statement = null;
		ResultSet rs;
		
		statement = conn.prepareStatement("SELECT Game.id, home.name, away.name, date FROM Game "
				+ "JOIN Team home ON home_id = home.id "
				+ "JOIN Team away ON away_id = away.id "
				+ "ORDER BY date ASC");
		rs = statement.executeQuery();
		ArrayList<Game> leftOverGames = new ArrayList<Game>();
		
		int count = 0;
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
				count++;
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
				
				// Update the game in the database
				updateGame(conn, id, game);
				count++;
				
				System.out.println("---------");
				System.out.println("Changed date");
				System.out.println(game);
				System.out.println("---------");
			}
		}
		
		System.out.println("Updated " + count + " games in the database");
			
		conn.close();
	}

	private void insertGame(java.sql.Connection conn, Game game) throws SQLException {
		System.out.println("Inserting " + game);
		
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
	private int getTeamID(java.sql.Connection conn, String teamName) throws SQLException {
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

	private void updateGame(java.sql.Connection conn, int id, Game game) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("UPDATE Game SET date=?, week=?, home_score=?, away_score=?, stadium=? "
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
		statement.setString(5, game.getStadium());
		statement.setInt(6, id);
		statement.executeUpdate();
	}

	private void insert() throws SQLException, IOException {
		java.sql.Connection conn = getConnection();
		for (Game game : games) {
			insertGame(conn, game);
		}
		System.out.println("Inserted " + games.size() + " games in the database");
		conn.close();
	}

	private boolean areGames() throws SQLException, IOException {
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
	
	private void scrape(int startMonth, int endMonth) {
		games = new ArrayList<Game>();
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
						return;
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
	}
	

}
