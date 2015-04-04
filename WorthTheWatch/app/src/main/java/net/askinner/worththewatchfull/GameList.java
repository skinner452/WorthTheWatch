package net.askinner.worththewatchfull;

import android.app.Activity;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;
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

    public ArrayList<Game> getAllGames() {
        ArrayList<Game> allGames = new ArrayList<Game>();
        for (ArrayList<Game> weekGames : games){
            for (Game game : weekGames){
                allGames.add(game);
            }
        }
        return allGames;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    public int getCurrentWeek (){
        Date date = new Date();

        for (int i = 0; i < games.size(); i++) {
            // If today is before the first game of the i week
            // Get the previous week
            if(date.before(games.get(i).get(0).getDate())){
                if(i == 0) {
                    return 0;
                } else {
                    return i - 1;
                }
            }
        }
        return games.size()-1;
    }

    public int gamesInWeek(int week){
        return games.get(week).size();
    }

    public int getMaxWeeks () {
        return games.size()-1;
    }

    public boolean areAllChecked (Activity activity, int week){
        for (Game game : games.get(week)){
            if(!game.isChecked(activity)){
                return false;
            }
        }
        return true;
    }

    private Team getTeam (String name) {
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

        Team homeTeam = getTeam(g.getHomeTeamName());
        Team awayTeam = getTeam(g.getAwayTeamName());

        if(g.isOver()){
            awayTeam.addRating(Double.parseDouble(g.getRating()));
            homeTeam.addRating(Double.parseDouble(g.getRating()));
        }

        g.setHomeTeam(homeTeam);
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
