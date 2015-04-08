package net.askinner.worththewatch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alec on 4/8/2015.
 */
public class ConnectionCheck {
    public static boolean hasConnection(Context context) {
        System.out.println("Testing connection");
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        if(cm.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED || cm.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED){
            try{
                return new SiteConnection().execute().get();
            } catch (Exception e){
                Toast.makeText(context, "Could not connect to host, try again later", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        Toast.makeText(context, "No connection, try to refresh", Toast.LENGTH_LONG).show();
        return false;
    }
}



class SiteConnection extends AsyncTask<Void,Void,Boolean> {
    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            URL url = new URL("http://askinner.net/wtw/connectionCheck.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Simple test method so the connection has to do something
            connection.getResponseCode();

            return true;
        } catch (Exception e){
            return false;
        }
    }
}