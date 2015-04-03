package net.askinner.worththewatch;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Alec on 4/2/2015.
 */
public class Table implements Comparator<Team>{
    ArrayList<Team> teams;

    public Table(Activity activity, GameList gameList){
        ArrayList<Game> games = gameList.getAllGames();
        teams = gameList.getTeams();

        for (Game game : games){
            // If this game is checked off for the user
            if(game.isChecked(activity) && game.hasScore()){
                Team homeTeam = game.getHomeTeam();
                Team awayTeam = game.getAwayTeam();
                int homeScore = game.getHomeScore();
                int awayScore = game.getAwayScore();

                boolean homeTeamFound = false;
                boolean awayTeamFound = false;
                for (Team team : teams){
                    if(team == homeTeam){
                        homeTeamFound = true;
                        team.addScore(homeScore, awayScore, true);
                        if(awayTeamFound){
                            break;
                        }
                    }

                    if(team == awayTeam){
                        awayTeamFound = true;
                        team.addScore(awayScore, homeScore, false);
                        if(homeTeamFound){
                            break;
                        }
                    }
                }
            }
        }

        Collections.sort(teams, this);
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    @Override
    public int compare(Team team2, Team team1) {
        // 1) points
        // 2) total wins
        // 3) total goal differential
        // 4) total goals scored
        if(team1.getPoints() > team2.getPoints()){
            return 1;
        } else if (team1.getPoints() < team2.getPoints()){
            return -1;
        } else {
            if(team1.getWins() > team2.getWins()){
                return 1;
            } else if (team1.getWins() < team2.getWins()){
                return -1;
            } else {
                if(team1.getGoalDifference() > team2.getGoalDifference()){
                    return 1;
                } else if (team1.getGoalDifference() < team2.getGoalDifference()){
                    return -1;
                } else {
                    if(team1.getTotalGoals() > team2.getTotalGoals()){
                        return 1;
                    } else if (team1.getTotalGoals() < team2.getTotalGoals()){
                        return -1;
                    } else {
                        return team2.getName().compareTo(team1.getName());
                    }
                }
            }
        }
    }
}
