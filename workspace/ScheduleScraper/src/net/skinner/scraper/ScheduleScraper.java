package net.skinner.scraper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScheduleScraper {
	public static void main(String[] args) {
		ScheduleScraper.run();
	}
	
	private static void run() {
		String baseURL = "http://www.mlssoccer.com/schedule";
		for (int month = 3; month <= 12; month++) {
			Connection conn = Jsoup.connect(baseURL + "?month=" + month);
			
			Document scheduleDoc = null;
			int retries = 0;
			while(scheduleDoc == null){
				try {
					scheduleDoc = conn.get();
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
			
			DateFormat dateFormat = new SimpleDateFormat("");
			for (int i = 0; i < scheduleTables.size(); i++) {
				String date = dates.get(i).text();
				Elements gameRows = scheduleTables.get(i).select("tbody").select("tr");
				System.out.println(date);
				for (Element gameRow : gameRows) {
					String competition = gameRow.select("span.competetion").text();
					if(competition.contains("MLS")){
						boolean playoffs;
						if(competition.contains("Playoffs") || competition.contains("Cup")){
							playoffs = true;
						}
						String time = gameRow.select("div.field-game-date-start-time").text();						
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
					}
				}
			}
				
		}
	}
}
