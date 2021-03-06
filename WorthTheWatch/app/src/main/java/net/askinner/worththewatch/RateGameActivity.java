package net.askinner.worththewatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class RateGameActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_game);

        final ListView charList = (ListView)findViewById(R.id.charList);
        final TextView ratingNum = (TextView)findViewById(R.id.ratingNum);
        final SeekBar ratingBar = (SeekBar)findViewById(R.id.ratingBar);
        Button submitButton = (Button)findViewById(R.id.submitButton);

        try{
            final ArrayList<String> charListArray = new RetrieveChars(getApplicationContext()).execute().get();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_checked,charListArray);
            charList.setAdapter(adapter);

            ratingNum.setText((ratingBar.getProgress()+1) + "");


            ratingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ratingNum.setText("" + (progress+1));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            final int gameID = getIntent().getIntExtra("gameID",0);


            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Post review
                    String deviceID = Settings.Secure.getString(v.getContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String chars = "";
                    SparseBooleanArray checkedItems = charList.getCheckedItemPositions();
                    for (int i = 0; i<charListArray.size();i++){
                        if(checkedItems.get(i)){
                            chars += charListArray.get(i);
                            chars += ";";
                        }
                    }
                    if(chars.length() > 0){
                        chars = chars.substring(0,chars.length()-1);
                    }

                    int rating = Integer.parseInt(ratingNum.getText() + "");
                    try{
                        boolean success = new PostReview(getApplicationContext()).execute("" + gameID, "" + rating, deviceID, chars).get();
                        if(success){
                            Toast.makeText(getApplicationContext(),"Thanks for your rating!",Toast.LENGTH_SHORT).show();

                            // Save that the user has already rated this game
                            SharedPreferences sharedPreferences = getSharedPreferences("rated",0);
                            sharedPreferences.edit().putBoolean(gameID + "", true).apply();

                            setResult(1);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),"There was a problem posting your rating",Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e){
                        Toast.makeText(getApplicationContext(),"There was a problem posting your rating",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e){

        }

        setTitle("Rate Game");
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

}

class RetrieveChars extends AsyncTask<Void,Void,ArrayList<String>> {
    private Context context;
    public RetrieveChars(Context context){
        this.context = context;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        ArrayList<String> chars = new ArrayList<String>();
        try {
            URL url = new URL("http://askinner.net/wtw/chars.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;

            while((line = in.readLine()) != null){
                chars.add(line);
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chars;
    }
}

class PostReview extends AsyncTask<String,Void,Boolean> {
    private Context context;
    public PostReview(Context context){
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            URL url = new URL("http://askinner.net/wtw/rategame.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");

            String parameters = "game_id=" + params[0] + "&rating=" + params[1] + "&device_id=" + params[2] + "&chars=" + params[3];

            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;

            String output = "";
            while((line = in.readLine()) != null){
                output += line;
            }

            in.close();

            if(output.equals("success")){
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "No connection, try again", Toast.LENGTH_LONG).show();
        return false;
    }
}