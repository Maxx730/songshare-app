package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private Intent songShareIntent;
    private String intentAction,intentType;
    private TextView SongTitleField,ArtistField;
    private MusicData musicData;
    private ImageView albumImage;
    private ProgressBar imageSpinner;
    private DataCommunicator commune;
    private Button ShareBtn;
    private RequestQueue req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        //Check if the user is already logged in or not.
        SharedPreferences check = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        if(!check.contains("SongShareUser") && !check.contains("SongSharePassword") && !check.contains("SongShareId")) {
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(i);
        }

        songShareIntent = getIntent();
        intentAction = songShareIntent.getAction();
        intentType = songShareIntent.getType();
        SongTitleField = findViewById(R.id.SongTitleField);
        ArtistField = findViewById(R.id.SongArtist);
        albumImage = findViewById(R.id.AlbumImage);
        imageSpinner = findViewById(R.id.ImageSpinner);
        commune = new DataCommunicator();
        ShareBtn = findViewById(R.id.ShareWithFriendsBtn);
        req = Volley.newRequestQueue(getApplicationContext());


        if(intentType != null && Intent.ACTION_SEND.equals(intentAction)){
            if("text/plain".equals(intentType)){
                musicData = GetSongAndArtist(songShareIntent.getStringExtra(Intent.EXTRA_TEXT));
                SongTitleField.setText(musicData.getTitle());
                ArtistField.setText(musicData.getArtist());
                SendAPIRequest(musicData.getArtist(),musicData.getTitle());

                ShareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StringRequest ShareTrack = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/share/create", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println(response);
                                try{
                                    JSONObject obj = new JSONObject(response);
                                    System.out.println(response);
                                    if(obj.getString("TYPE").equals("SUCCESS")){
                                        Toast.makeText(getApplicationContext(),"Track Shared",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }else{
                                        Toast.makeText(getApplicationContext(),"There seems to have been an issue sharing this track.",Toast.LENGTH_SHORT).show();
                                    }
                                }catch(Exception e){
                                    System.out.println("ERROR SHARING MEDIA");
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
                                Map<String,String> params = new HashMap<String, String>();

                                params.put("_id",Integer.toString(prefs.getInt("SongShareId",0)));
                                params.put("title", SongTitleField.getText().toString());
                                params.put("artist", ArtistField.getText().toString());

                                return params;
                            }
                        };
                        req.add(ShareTrack);
                    }
                });
            }
        }
    }

    private MusicData GetSongAndArtist(String url){
        String sections[] = url.split("=");
        String SongArtist[] = sections[1].split("-");

        System.out.println("SONG: "+SongArtist[0]+" ARTIST:"+SongArtist[1]);

        MusicData musicData = new MusicData(SongArtist[0].replace("_"," ").substring(0,SongArtist[0].length() - 1),SongArtist[1].replace("_"," ").substring(1,SongArtist[1].length()));
        return musicData;
    }

    private void SendAPIRequest(String artist,String track){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=d416d5b04ba1b47bf4aad5f6123889ba&artist="+artist.replace(" ","%20")+"&track="+track.replace(" ","%20")+"&format=json";
        System.out.println(url);
        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(null != response){

                            JSONObject track = null;
                            try {
                                track = response.getJSONObject("track");
                                JSONObject album = null;

                                try{
                                    album = track.getJSONObject("album");
                                    JSONArray covers = album.getJSONArray("image");
                                    JSONObject largeImage = covers.getJSONObject(2);
                                    musicData.setArt(largeImage.getString("#text"));
                                    AlbumArtLoader load = new AlbumArtLoader(albumImage,largeImage.getString("#text"),imageSpinner);
                                    load.execute("");
                                }catch (JSONException e){
                                    System.out.println("ALERT: No album information.");
                                }
                            } catch (JSONException e) {
                                System.out.println("ALERT: Track not found.");
                            }
                    }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(req);
    }

}
