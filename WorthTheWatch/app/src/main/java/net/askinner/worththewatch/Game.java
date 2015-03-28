package net.askinner.worththewatch;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alec on 3/24/2015.
 */
public class Game implements Serializable {

    private int id;
    private Date date;
    private String homeTeamName;
    private String awayTeamName;
    private Team homeTeam;
    private Team awayTeam;
    private Integer homeScore;
    private Integer awayScore;
    private String stadium;
    private boolean isPlayoffs;
    private String[] channels;
    private int week;
    private boolean checked;

    public Game(String line) {
        String[] lineSplit = line.split(":");
        id = Integer.parseInt(lineSplit[0]);
        date = new Date(Long.parseLong(lineSplit[1]));
        homeTeamName = lineSplit[2];
        awayTeamName = lineSplit[3];
        try{
            homeScore = Integer.parseInt(lineSplit[4]);
            awayScore = Integer.parseInt(lineSplit[5]);
        } catch (Exception e){
            homeScore = null;
            awayScore = null;
        }

        stadium = lineSplit[6];
        isPlayoffs = Boolean.parseBoolean(lineSplit[7]);
        channels = lineSplit[8].split(";");
        week = Integer.parseInt(lineSplit[9]);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String[] getChannels() {
        return channels;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean hasScore() {
        if(homeScore != null){
            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public int getWeek() {
        return week;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public String getStadium() {
        return stadium;
    }

    public Date getDate() {
        return date;
    }

    public String getFormattedDate() {
        DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        String dateFormatted = dateFormat.format(date);
        return dateFormatted;
    }

    public String getFormattedTime() {
        DateFormat timeFormat = new SimpleDateFormat("h:mm a");
        String timeFormatted = timeFormat.format(date);
        return timeFormatted;
    }

    public String getFormattedChannels() {
        String output = "";
        for (int i = 0; i < channels.length; i++){
            output += channels[i];
            if(i != channels.length-1){
                output += ", ";
            }
        }
        return output;
    }
}
