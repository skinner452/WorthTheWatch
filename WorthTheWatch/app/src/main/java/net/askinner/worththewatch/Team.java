package net.askinner.worththewatch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Alec on 3/26/2015.
 */
public class Team implements Serializable{

    private String name;
    private Bitmap logo;
    private boolean noLogo;

    public Team(String name) {
        this.name = name;
        logo = null;
    }

    public String getName() {
        return name;
    }

    public void setLogo(Bitmap logo) {
        this.logo = logo;
        if(logo == null){
            noLogo = true;
        }
    }

    public void putLogo(ImageView imageView){
        if(!noLogo){
            if(logo == null){
                new LogoDownloader(imageView, this).execute("http://askinner.net/wtw/logos/" + name + ".png");
            } else {
                imageView.setImageBitmap(logo);
            }
        }
    }
}
