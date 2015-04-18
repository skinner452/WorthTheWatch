package net.askinner.worththewatch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Alec on 3/24/2015.
 */
public class GameAdapter extends BaseAdapter {
    private GameList gameList;
    private Context context;
    private int defaultTextColor;
    private int defaultBackgroundColor;

    public GameAdapter(Context context, GameList gameList){
        this.gameList = gameList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return gameList.getGames().size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GameViewHolder viewHolder;

        if (convertView == null) { // no view to re-use, create new
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.game_item, parent, false);

            viewHolder = new GameViewHolder();
            viewHolder.homeName = (TextView)convertView.findViewById(R.id.homeTeam);
            viewHolder.awayName = (TextView)convertView.findViewById(R.id.awayTeam);
            viewHolder.dateText = (TextView)convertView.findViewById(R.id.date);
            viewHolder.timeText = (TextView)convertView.findViewById(R.id.time);
            viewHolder.scoreText = (TextView)convertView.findViewById(R.id.score);
            viewHolder.stadium = (TextView)convertView.findViewById(R.id.stadium);
            viewHolder.homeLogo = (ImageView)convertView.findViewById(R.id.home_logo);
            viewHolder.awayLogo = (ImageView)convertView.findViewById(R.id.away_logo);
            viewHolder.watchedCheck = (CheckBox)convertView.findViewById(R.id.watched_check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GameViewHolder)convertView.getTag();
        }
        if(defaultTextColor == 0){
            defaultTextColor = viewHolder.scoreText.getCurrentTextColor();
        }

        if(defaultBackgroundColor == 0){
            defaultBackgroundColor = viewHolder.scoreText.getDrawingCacheBackgroundColor();
        }


        final Game game = gameList.getGames().get(position);

        viewHolder.homeName.setText(game.getHomeTeamName());
        viewHolder.awayName.setText(game.getAwayTeamName());

        viewHolder.watchedCheck.setChecked(game.isChecked((Activity)context));

        viewHolder.dateText.setText(game.getFormattedDate());
        viewHolder.timeText.setText(game.getFormattedTime());

        viewHolder.watchedCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(!gameList.isUpdating()){
                        gameList.setHasChanged(true);

                        game.setChecked(viewHolder.watchedCheck.isChecked(), (Activity)context);

                        setScoreText(viewHolder, game);
                    }
                } catch (Exception e){

                }
            }
        });

        setScoreText(viewHolder, game);

        viewHolder.stadium.setText(game.getStadium());

        // Home team logo
        Team homeTeam = game.getHomeTeam();
        homeTeam.putLogo(viewHolder.homeLogo);

        // Away team logo
        Team awayTeam = game.getAwayTeam();
        awayTeam.putLogo(viewHolder.awayLogo);


        return convertView;
    }

    private void setScoreText(GameViewHolder viewHolder, Game game) {
        if (viewHolder.watchedCheck.isChecked() && game.getHomeScore() != null) {
            viewHolder.scoreText.setText(game.getHomeScore() + " - " + game.getAwayScore());
            viewHolder.scoreText.setTextColor(defaultTextColor);
            viewHolder.scoreText.setBackgroundColor(defaultBackgroundColor);
        } else {
            viewHolder.scoreText.setText(game.getRating());

            if(game.isOver()){
                double rating = Double.parseDouble(game.getRating());

                int red = 255;
                int green = 255;
                if(rating > 5){
                    red = (int)Math.round(1-((rating - 5)/5) * 255);
                } else if (rating < 5){
                    green = (int)Math.round(rating/5 * 255);
                }
                int color = Color.rgb(red, green, 0);
                viewHolder.scoreText.setBackgroundColor(Color.DKGRAY);
                viewHolder.scoreText.setTextColor(color);
            } else {
                viewHolder.scoreText.setTextColor(defaultTextColor);
                viewHolder.scoreText.setBackgroundColor(defaultBackgroundColor);
            }

        }
    }

    public void checkAll(boolean b) {
        for (Game g : gameList.getGames()){
            g.setChecked(b,(Activity)context);
        }
        notifyDataSetChanged();
    }

    public void update() {
        System.out.println("GameAdapter update");
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return gameList.getGames().get(position);
    }
}

class GameViewHolder {
    TextView homeName;
    TextView awayName;
    TextView dateText;
    TextView timeText;
    TextView scoreText;
    TextView stadium;
    ImageView homeLogo;
    ImageView awayLogo;
    CheckBox watchedCheck;
}