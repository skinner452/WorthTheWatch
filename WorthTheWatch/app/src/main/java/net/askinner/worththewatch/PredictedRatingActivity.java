package net.askinner.worththewatch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;


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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_predicted_rating, menu);
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
