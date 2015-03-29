package net.askinner.worththewatch;

import android.content.Context;
import android.content.res.Resources;
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
public class Team {

    private String name;
    private Bitmap logo;
    private LogoDownloader ld;
    private double averageRating;

    public Team(String name) {
        this.name = name;
        logo = null;
        ld = null;
    }

    public String getName() {
        return name;
    }

    public void putLogo(ImageView imageView){
        Context context = imageView.getContext();
        if(logo == null){
            String fileName = name.replace(" ","").replace(".","").toLowerCase();
            int rID = context.getResources().getIdentifier(fileName,"drawable",context.getPackageName());
            logo = BitmapFactory.decodeResource(context.getResources(),rID);

            if(logo == null){
                logo = BitmapFactory.decodeResource(context.getResources(),android.R.drawable.ic_menu_gallery);
            }
        }

        imageView.setImageBitmap(logo);

//        if(!noLogo){
//            if(logo == null){
//                try{
//                    logo = new LogoDownloader().execute(name).get();
//                    if(logo == null){
//                        noLogo = true;
//                    } else {
//                        imageView.setImageBitmap(logo);
//                    }
//                } catch (Exception e){
//                    noLogo = true;
//                }
//            } else {
//                imageView.setImageBitmap(logo);
//            }
//        }
    }
}
