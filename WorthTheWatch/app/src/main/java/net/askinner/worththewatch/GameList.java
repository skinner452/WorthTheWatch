package net.askinner.worththewatch;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Alec on 3/24/2015.
 */
public class GameList{
    private ArrayList<ArrayList<Game>> games; // Arraylist of weeks which contain an arraylist of games
    private ArrayList<Team> teams;
    private int currentWeek;
    private boolean gotWeek;
    private boolean hasChanged;
    private Table table;



    public GameList() {
        games = new ArrayList<ArrayList<Game>>();
        teams = new ArrayList<Team>();
        table = null;
    }

    public void resetTeams() {
        for (Team team : teams){
            team.clearResults();
        }
    }

    public void clear() {
        games.clear();
    }

    public boolean isEmpty() {
        return games.isEmpty();
    }

    public void update() {
        System.out.println("Updating gameList");
    }

    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public Table getTable(Activity activity) {
        if(table == null || hasChanged){
            table = new Table(activity, this);
        }
        return table;
    }

    // If the GameListFragment is getting games for the first time,
    // we want this method to get the current week
    public ArrayList<Game> getGames (){
        if(!gotWeek){
            currentWeek = getStartWeek();
            gotWeek = true;
        }
        return games.get(currentWeek);
    }

    public boolean nextWeek() {
        if(currentWeek < getMaxWeeks()){
            currentWeek++;
            return true;
        }
        return false;
    }

    public boolean backWeek() {
        if(currentWeek > 0){
            currentWeek--;
            return true;
        }
        return false;
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

    public String weekString() {
        return "Week " + (currentWeek+1);
    }

    public int getStartWeek (){
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

    public int getMaxWeeks () {
        return games.size()-1;
    }

    public boolean areAllChecked (Activity activity){
        for (Game game : games.get(currentWeek)){
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
            awayTeam.addRating(g.getAverageRating());
            homeTeam.addRating(g.getAverageRating());
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
}

class RetrieveGames extends AsyncTask<Void,Void,GameList> {
    private Fragment fragment;
    private GameList gameList;

    public RetrieveGames(Fragment fragment, GameList gameList){
        this.fragment = fragment;
        this.gameList = gameList;
        if(!gameList.isEmpty()){
            gameList.clear();
        }
    }

    @Override
    protected void onPreExecute() {
        if(fragment instanceof GameListFragment){
            ((GameListFragment) fragment).setViewComponents(false);
        }

        if(fragment instanceof YourTableFragment){
            ((YourTableFragment) fragment).setViewComponents(false);
        }
    }

    @Override
    protected GameList doInBackground(Void... params) {
        System.out.println("Retrieving games");
        try {
            URL url = new URL("http://askinner.net/wtw/games.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;

            while((line = in.readLine()) != null){
                gameList.addLine(line);
            }

            in.close();
        } catch (MalformedURLException e) {
            Toast.makeText(fragment.getActivity().getApplicationContext(), "No connection, try again", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(fragment.getActivity().getApplicationContext(), "No connection, try again", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return gameList;
    }

    @Override
    protected void onPostExecute(GameList gameList) {
        if(fragment instanceof GameListFragment){
            ((GameListFragment) fragment).setViewComponents(true);
            ((GameListFragment) fragment).updateAdapter();
        }

        if(fragment instanceof  YourTableFragment){
            ((YourTableFragment) fragment).setViewComponents(true);
            ((YourTableFragment) fragment).updateAdapter();
        }
    }
}
