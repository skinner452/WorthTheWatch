package net.askinner.worththewatch;

import android.app.Activity;
import android.content.Context;
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
            viewHolder.dateText = (TextView)convertView.findViewById(R.id.date);
            viewHolder.timeText = (TextView)convertView.findViewById(R.id.time);
            viewHolder.scoreText = (TextView)convertView.findViewById(R.id.score);
            viewHolder.stadiumText = (TextView)convertView.findViewById(R.id.stadium);
            viewHolder.homeLogo = (ImageView)convertView.findViewById(R.id.home_logo);
            viewHolder.awayLogo = (ImageView)convertView.findViewById(R.id.away_logo);
            viewHolder.watchedCheck = (CheckBox)convertView.findViewById(R.id.watched_check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GameViewHolder)convertView.getTag();
        }

        final Game game = gameList.getGames().get(position);
        viewHolder.watchedCheck.setChecked(game.isChecked((Activity)context));

        viewHolder.dateText.setText(game.getFormattedDate());
        viewHolder.timeText.setText(game.getFormattedTime());

        viewHolder.watchedCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameList.setHasChanged(true);

                game.setChecked(viewHolder.watchedCheck.isChecked(), (Activity)context);

                if (viewHolder.watchedCheck.isChecked() && game.getHomeScore() != null) {
                    viewHolder.scoreText.setText(game.getHomeScore() + " - " + game.getAwayScore());
                } else {
                    viewHolder.scoreText.setText(game.getRating());
                }
            }
        });

        if (viewHolder.watchedCheck.isChecked() && game.getHomeScore() != null) {
            viewHolder.scoreText.setText(game.getHomeScore() + " - " + game.getAwayScore());
        } else {
            viewHolder.scoreText.setText(game.getRating());
        }

        viewHolder.stadiumText.setText(game.getStadium());

        // Home team logo
        Team homeTeam = game.getHomeTeam();
        homeTeam.putLogo(viewHolder.homeLogo);

        // Away team logo
        Team awayTeam = game.getAwayTeam();
        awayTeam.putLogo(viewHolder.awayLogo);


        return convertView;
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
    TextView dateText;
    TextView timeText;
    TextView scoreText;
    TextView stadiumText;
    ImageView homeLogo;
    ImageView awayLogo;
    CheckBox watchedCheck;
}