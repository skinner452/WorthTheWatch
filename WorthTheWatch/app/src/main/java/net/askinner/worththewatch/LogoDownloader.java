package net.askinner.worththewatch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.InputStream;

/**
 * Created by Alec on 3/27/2015.
 */
public class LogoDownloader extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView;
    private Team team;

    public LogoDownloader (ImageView imageView, Team team){
        this.imageView = imageView;
        this.team = team;
    }
    @Override
    protected Bitmap doInBackground(String... params) {
        System.out.println("Getting logo: " + params[0]);
        params[0] = params[0].replace(" ","%20");
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(params[0]);
        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("LogoDownloader", "Error " + statusCode
                        + " while retrieving bitmap from " + params[0]);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or
            // IllegalStateException
            getRequest.abort();
            Log.w("LogoDownloader", "Error while retrieving bitmap from " + params[0]);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        team.setLogo(bitmap);
        super.onPostExecute(bitmap);
    }
}
