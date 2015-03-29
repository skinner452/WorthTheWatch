package net.askinner.worththewatch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


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

class GetAverageRating extends AsyncTask<Integer,Void,Boolean> {
    private TextView averageRating;
    private TextView numRatings;
    private TextView char1;
    private TextView percent1;
    private TextView char2;
    private TextView percent2;
    private TextView char3;
    private TextView percent3;

    public GetAverageRating(TextView averageRating, TextView numRatings, TextView char1, TextView percent1, TextView char2, TextView percent2,
                            TextView char3, TextView percent3){
        this.averageRating = averageRating;
        this.numRatings = numRatings;
        this.char1 = char1;
        this.percent1 = percent1;
        this.char2 = char2;
        this.percent2 = percent2;
        this.char3 = char3;
        this.percent3 = percent3;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        try {
            URL url = new URL("http://askinner.net/wtw/gameaverageratingdetailed.php?game_id=" + params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;

            // first line is avg. rating
            line = in.readLine();
            Double averageRating = Double.parseDouble(line);
            this.averageRating.setText(String.format("%.2f",averageRating));

            HashMap<String, Integer> topChars = new HashMap<String, Integer>();
            while((line = in.readLine()) != null){
                String[] lineSplit = line.split(";");
                for (int i = 0; i < lineSplit.length ; i++){
                    String c = lineSplit[i];
                    if(topChars.containsKey(c)){
                        topChars.put(c,topChars.get(c)+1);
                    } else {
                        topChars.put(c,1);
                    }
                }
            }

            String[] topCs = new String[3];
            Integer[] topRs = new Integer[3];
            for (int i = 0; i < 3; i++){
                String topC = "";
                int topR = 0;
                for(String c : topChars.keySet()){
                    int r = topChars.get(c);
                    if(r > topR){
                        topC = c;
                        topR = r;
                    }
                }
                topCs[i] = topC;
                topRs[i] = topR;

                topChars.remove(topC);
            }


            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}