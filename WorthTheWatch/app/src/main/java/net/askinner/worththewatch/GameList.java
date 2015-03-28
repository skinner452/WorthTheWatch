package net.askinner.worththewatch;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alec on 3/24/2015.
 */
public class GameList{
    private ArrayList<ArrayList<Game>> games; // Arraylist of weeks which contain an arraylist of games
    private ArrayList<Team> teams;
    private ArrayList<Boolean> checked;
    private HashMap<String,Bitmap> teamLogos;

    public GameList() {
        games = new ArrayList<ArrayList<Game>>();
        teams = new ArrayList<Team>();
        checked = new ArrayList<Boolean>();
        teamLogos = new HashMap<String,Bitmap>();
    }

    public int gamesInWeek(int week){
        return games.get(week).size();
    }

    public int getMaxWeeks () {
        return games.size()-1;
    }

    public void setChecked(int week, Boolean b){
        for (Game game : games.get(week)){
            game.setChecked(b);
        }
    }

    public boolean hasLogo(String teamName){
        return teamLogos.containsKey(teamName);
    }

    public boolean addTeamLogo(String teamName, Bitmap logo) {
        teamLogos.put(teamName, logo);
        return true;
    }

    private Team addTeam (String name) {
        for (Team team : teams){
            if(team.getName().equals(name)){
                return team;
            }
        }

        Team team = new Team(name);
        teams.add(team);
        return team;
    }

    public void addLine(String line) {
        Game g = new Game(line);

        Team homeTeam = addTeam(g.getHomeTeamName());
        g.setHomeTeam(homeTeam);

        Team awayTeam = addTeam(g.getAwayTeamName());
        g.setAwayTeam(awayTeam);

        int week = g.getWeek();
        try{
            games.get(week).add(g);
        } catch (Exception e) {
            games.add(week,new ArrayList<Game>());
            games.get(week).add(g);
        }
    }

    public ArrayList<Game> getGames (int week){
        return games.get(week);
    }
}
