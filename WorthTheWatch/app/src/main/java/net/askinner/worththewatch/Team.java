package net.askinner.worththewatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * Created by Alec on 3/26/2015.
 */
public class Team{

    private String name;
    private Bitmap logo;
    private double averageRating;
    private double totalRating;
    private double numRatings;

    // For use in the table
    private int wins;
    private int draws;
    private int losses;
    private int homeGoalsFor;
    private int homeGoalsAgainst;
    private int awayGoalsFor;
    private int awayGoalsAgainst;

    private char division;

    public Team(String name) {
        this.name = name;
        logo = null;

        // temporary for divisions
        switch (name){
            case "Chicago":
                division = 'E';
                break;
            case "Colorado":
                division = 'W';
                break;
            case "Columbus":
                division = 'E';
                break;
            case "D.C. United":
                division = 'E';
                break;
            case "FC Dallas":
                division = 'W';
                break;
            case "Houston":
                division = 'W';
                break;
            case "LA Galaxy":
                division = 'W';
                break;
            case "Montreal":
                division = 'E';
                break;
            case "New England":
                division = 'E';
                break;
            case "New York City":
                division = 'E';
                break;
            case "NY Red Bulls":
                division = 'E';
                break;
            case "Orlando":
                division = 'E';
                break;
            case "Philadelphia":
                division = 'E';
                break;
            case "Portland":
                division = 'W';
                break;
            case "Real Salt Lake":
                division = 'W';
                break;
            case "San Jose":
                division = 'W';
                break;
            case "Seattle":
                division = 'W';
                break;
            case "Sporting KC":
                division = 'W';
                break;
            case "Toronto FC":
                division = 'E';
                break;
            case "Vancouver":
                division = 'W';
                break;
        }
    }

    public char getDivision() {
        return division;
    }

    public void addRating(Double rating){

        if(rating > 0){
            numRatings++;
            totalRating += rating;
            averageRating = totalRating/numRatings;
        }
    }

    public String getName() {
        return name;
    }

    public void putLogo(ImageView imageView){
        Context context = imageView.getContext();
        if(logo == null){
            String fileName = name.replace(" ","").replace(".","").toLowerCase();
            int rID = context.getResources().getIdentifier(fileName,"drawable",context.getPackageName());
            logo = BitmapFactory.decodeResource(context.getResources(),rID);

            if(logo == null){
                logo = BitmapFactory.decodeResource(context.getResources(),android.R.drawable.ic_menu_gallery);
            }
        }

        imageView.setImageBitmap(logo);
    }

    public double getAverageRating() {
        if(averageRating==0){
            return 5.0;
        }
        return averageRating;
    }

    public String getFormattedAverageRating() {
        return String.format("%.2f",getAverageRating());
    }

    public void addScore(int goalsFor, int goalsAgainst, boolean home){
        if(home){
            homeGoalsFor += goalsFor;
            homeGoalsAgainst += goalsAgainst;
        } else {
            awayGoalsFor += goalsFor;
            awayGoalsAgainst += goalsAgainst;
        }

        if(goalsFor > goalsAgainst){
            wins ++;
        } else if (goalsFor == goalsAgainst){
            draws ++;
        } else {
            losses ++;
        }
    }

    public String tableString () {
        return name + ": " + getGamesPlayed() + " " + getWins() + " " + getDraws() + " " + getLosses() + " " + getGoalsFor() + " " + getGoalsAgainst() + " " + getGoalDifference() + " " + getPoints();
    }

    public int getGamesPlayed() {
        return wins + draws + losses;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getGoalsFor() {
        return homeGoalsFor + awayGoalsFor;
    }

    public int getHomeGoalsFor() {
        return homeGoalsFor;
    }

    public int getHomeGoalsAgainst() {
        return homeGoalsAgainst;
    }

    public int getGoalsAgainst() {
        return homeGoalsAgainst + awayGoalsAgainst;
    }

    public int getAwayGoalsFor() {
        return awayGoalsFor;
    }

    public int getAwayGoalsAgainst() {
        return awayGoalsAgainst;
    }

    public int getGoalDifference() {
        return getGoalsFor() - getGoalsAgainst();
    }

    public int getPoints() {
        return getWins()*3 + getDraws();
    }

    public int getTotalGoals() {
        return getHomeGoalsFor() + getAwayGoalsFor();
    }
}
