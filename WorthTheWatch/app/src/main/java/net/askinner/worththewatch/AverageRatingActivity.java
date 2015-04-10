package net.askinner.worththewatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

        setTitle(homeTeam + " - " + awayTeam);


        Button submitReview = (Button) findViewById(R.id.submitReviewButton);
        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check to see if they have already rated the game before
                SharedPreferences sharedPreferences = getSharedPreferences("rated",0);
                if(!sharedPreferences.getBoolean("" + gameID, false)){
                    if(ConnectionCheck.hasConnection(getApplicationContext())){
                        Intent intent = new Intent(v.getContext(),RateGameActivity.class);
                        intent.putExtra("gameID",gameID);
                        startActivityForResult(intent, GameListFragment.NEEDS_UPDATE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You have already rated this game", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setDetails();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GameListFragment.NEEDS_UPDATE){
            if(resultCode == 1){
                setDetails();
                setResult(1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setDetails() {
        final int gameID = getIntent().getIntExtra("gameID",0);

        TextView averageRating = (TextView)findViewById(R.id.averageRating);
        TextView numRatings = (TextView)findViewById(R.id.numRatings);
        TextView char1 = (TextView)findViewById(R.id.char1);
        TextView percent1 = (TextView)findViewById(R.id.percent1);
        TextView char2 = (TextView)findViewById(R.id.char2);
        TextView percent2 = (TextView)findViewById(R.id.percent2);
        TextView char3 = (TextView)findViewById(R.id.char3);
        TextView percent3 = (TextView)findViewById(R.id.percent3);
        try{
            String[] items = new AverageRatingDetails().execute(gameID).get();
            averageRating.setText(items[0]);
            numRatings.setText(items[1]);
            char1.setText(items[2]);
            percent1.setText(items[3]);
            char2.setText(items[4]);
            percent2.setText(items[5]);
            char3.setText(items[6]);
            percent3.setText(items[7]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

class AverageRatingDetails extends AsyncTask<Integer,Void,String[]> {

    @Override
    protected String[] doInBackground(Integer... params) {
        String[] output = new String[8];
        try {
            URL url = new URL("http://askinner.net/wtw/gameaverageratingdetailed.php?game_id=" + params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;

            // first line is avg. rating
            line = in.readLine();

            Double averageRating;
            try{
                averageRating = Double.parseDouble(line);
            } catch (Exception e){
                averageRating = 5.0;
            }

            output[0] = String.format("%.2f",averageRating);

            HashMap<String, Integer> topChars = new HashMap<String, Integer>();

            int numRatings = 0;
            while((line = in.readLine()) != null){
                numRatings++;

                String[] lineSplit = line.split(";");
                for (int i = 0; i < lineSplit.length ; i++){
                    String c = lineSplit[i];
                    if(!c.equals("")){
                        if(topChars.containsKey(c)){
                            topChars.put(c,topChars.get(c)+1);
                        } else {
                            topChars.put(c,1);
                        }
                    }
                }
            }

            output[1] = numRatings + " Ratings";


            if(numRatings > 0){
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

                if(!topCs[0].equals("")){
                    output[2] = topCs[0];
                    Double p = ((double)topRs[0]/(double)numRatings)*100;
                    output[3] = String.format("%.2f%%",p);

                    if(!topCs[1].equals("")){
                        output[4] = topCs[1];
                        p = ((double)topRs[1]/(double)numRatings)*100;
                        output[5] = String.format("%.2f%%",p);

                        if(!topCs[2].equals("")){
                            output[6] = topCs[2];
                            p = ((double)topRs[2]/(double)numRatings)*100;
                            output[7] = String.format("%.2f%%",p);
                        }
                    }
                }

            }


            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }
}