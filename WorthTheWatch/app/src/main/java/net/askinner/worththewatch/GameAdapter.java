package net.askinner.worththewatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Alec on 3/24/2015.
 */
public class GameAdapter extends BaseAdapter {
    private GameList gameList;
    private int week;
    private Context context;

    public GameAdapter(Context context, GameList gameList, int week){
        this.gameList = gameList;
        this.week = week;
        this.context = context;
    }

    @Override
    public int getCount() {
        return gameList.gamesInWeek(week);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) { // no view to re-use, create new
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.game_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.dateText = (TextView)convertView.findViewById(R.id.date);
            viewHolder.timeText = (TextView)convertView.findViewById(R.id.time);
            viewHolder.scoreText = (TextView)convertView.findViewById(R.id.score);
            viewHolder.stadiumText = (TextView)convertView.findViewById(R.id.stadium);
            viewHolder.homeLogo = (ImageView)convertView.findViewById(R.id.home_logo);
            viewHolder.awayLogo = (ImageView)convertView.findViewById(R.id.away_logo);
            viewHolder.watchedCheck = (CheckBox)convertView.findViewById(R.id.watched_check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        final Game game = gameList.getGames(week).get(position);
        viewHolder.watchedCheck.setChecked(game.isChecked());

        viewHolder.dateText.setText(game.getFormattedDate());
        viewHolder.timeText.setText(game.getFormattedTime());

        viewHolder.watchedCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.setChecked(viewHolder.watchedCheck.isChecked());

                if (viewHolder.watchedCheck.isChecked()) {
                    if (game.getHomeScore() != null) {
                        viewHolder.scoreText.setText(game.getHomeScore() + " - " + game.getAwayScore());
                    }
                } else {
                    // Place holder for the rating
                    viewHolder.scoreText.setText("[rating]");
                }
            }
        });

        if (viewHolder.watchedCheck.isChecked()) {
            if (game.getHomeScore() != null) {
                viewHolder.scoreText.setText(game.getHomeScore() + " - " + game.getAwayScore());
            }
        } else {
            viewHolder.scoreText.setText("[rating]");
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return gameList.getGames(week).get(position);
    }
}

class ViewHolder {
    TextView dateText;
    TextView timeText;
    TextView scoreText;
    TextView stadiumText;
    ImageView homeLogo;
    ImageView awayLogo;
    CheckBox watchedCheck;
}