package net.askinner.worththewatch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class AverageRatingActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_rating);

        final int gameID = getIntent().getIntExtra("gameID",0);

        TextView date = (TextView) findViewById(R.id.date);
        date.setText(getIntent().getStringExtra("date"));

        TextView time = (TextView) findViewById(R.id.time);
        time.setText(getIntent().getStringExtra("time"));

        TextView stadium = (TextView) findViewById(R.id.stadium);
        stadium.setText(getIntent().getStringExtra("stadium"));

        TextView channels = (TextView) findViewById(R.id.channels);
        channels.setText(getIntent().getStringExtra("channels"));

        String homeTeam = getIntent().getStringExtra("homeTeam");

        String awayTeam = getIntent().getStringExtra("awayTeam");

        ImageView homeLogo = (ImageView) findViewById(R.id.homeLogo);
        new Team(homeTeam).putLogo(homeLogo);

        ImageView awayLogo = (ImageView) findViewById(R.id.awayLogo);
        new Team(awayTeam).putLogo(awayLogo);

        Button submitReview = (Button) findViewById(R.id.submitReviewButton);
        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),RateGameActivity.class);
                intent.putExtra("gameID",gameID);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_average_rating, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
