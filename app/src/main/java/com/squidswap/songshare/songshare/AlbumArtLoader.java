package com.squidswap.songshare.songshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlbumArtLoader extends AsyncTask<String,Void,Bitmap>{

    private String url;
    private ImageView view;
    private ProgressBar spinner;

    public AlbumArtLoader(ImageView img, String string,ProgressBar spinner) {
        this.url = string;
        this.view = img;
        this.spinner = spinner;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        System.out.println("ALERT: Loading album art...");
        try{
            URL source = new URL(this.url);
            HttpURLConnection con = (HttpURLConnection) source.openConnection();

            con.setDoInput(true);
            con.connect();

            InputStream inp = con.getInputStream();
            Bitmap b = BitmapFactory.decodeStream(inp);

            return b;
        } catch(IOException e){
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        this.view.setImageBitmap(bitmap);
        this.spinner.setVisibility(View.GONE);
        this.view.setVisibility(View.VISIBLE);
    }
}
