package net.askinner.worththewatch;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


public class PredictedRatingActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predicted_rating);

        TextView date = (TextView) findViewById(R.id.date);
        date.setText(getIntent().getStringExtra("date"));

        TextView time = (TextView) findViewById(R.id.time);
        time.setText(getIntent().getStringExtra("time"));

        TextView stadium = (TextView) findViewById(R.id.stadium);
        stadium.setText(getIntent().getStringExtra("stadium"));

        TextView channels = (TextView) findViewById(R.id.channels);
        channels.setText(getIntent().getStringExtra("channels"));

        TextView homeTeamName = (TextView) findViewById(R.id.homeName);
        String homeTeam = getIntent().getStringExtra("homeTeam");
        homeTeamName.setText(homeTeam);

        TextView awayTeamName = (TextView) findViewById(R.id.awayName);
        String awayTeam = getIntent().getStringExtra("awayTeam");
        awayTeamName.setText(awayTeam);

        ImageView homeLogo = (ImageView) findViewById(R.id.homeLogo);
        new Team(homeTeam).putLogo(homeLogo);

        ImageView awayLogo = (ImageView) findViewById(R.id.awayLogo);
        new Team(awayTeam).putLogo(awayLogo);

        TextView homeAverage = (TextView) findViewById(R.id.homeAverage);
        homeAverage.setText(getIntent().getStringExtra("homeAverage"));

        TextView awayAverage = (TextView) findViewById(R.id.awayAverage);
        awayAverage.setText(getIntent().getStringExtra("awayAverage"));

        TextView predicted = (TextView) findViewById(R.id.predictedRating);
        predicted.setText(getIntent().getStringExtra("predicted"));

        setTitle(homeTeam + " - " + awayTeam);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }
}
