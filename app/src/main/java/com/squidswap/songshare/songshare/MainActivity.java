package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.api.Authentication;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

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
    private AuthenticationRequest spotReq;
    private String spotID = "11dFghVXANMlKmJXsNCbNl";

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

                String songData = songShareIntent.getStringExtra(Intent.EXTRA_TEXT);

                if(songData.indexOf("open.spotify.com") > -1){
                    Log.d("FULL URL",songData);
                    Log.d("FOUND","SPOTIFY LINK");
                    spotID = songData.split("track/")[1];
                    final int REQUEST_CODE = 1337;
                    final String REDIRECT_URI = "songshare://callback";

                    AuthenticationRequest.Builder builder =
                            new AuthenticationRequest.Builder("80ae6b13fb764c9e80d8a11dbb34525d", AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

                    builder.setScopes(new String[]{"user-read-private"});
                    AuthenticationRequest request = builder.build();

                    AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

                }else if(songData.indexOf("youtu.be") > -1){
                    final String youtubeID = songData.split("youtu.be")[1].substring(1);
                    StringRequest youtubeDataReq = new StringRequest(Request.Method.GET, "https://www.googleapis.com/youtube/v3/videos?part=id%2C+snippet&id="+youtubeID+"&key=AIzaSyDzZKCdu3xvwIdPWA7aLhpGLCxaahP5G0U", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                Log.d("FROM YOUTUBE",response);
                                final JSONObject responseObj = new JSONObject(response);
                                SongTitleField.setText(responseObj.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("title"));
                                ArtistField.setVisibility(View.GONE);
                                AlbumArtLoader load = new AlbumArtLoader(albumImage,responseObj.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"),imageSpinner);
                                load.execute("");

                                ShareBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        StringRequest youtubeShare = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/share/create", new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try{
                                                    JSONObject responseObj = new JSONObject(response);

                                                    if(responseObj.getString("TYPE").equals("SUCCESS")){
                                                        Toast.makeText(getApplicationContext(),"Youtube video shared...",Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }else{
                                                        Toast.makeText(getApplicationContext(),"Error sharing Youtube video...",Toast.LENGTH_SHORT).show();
                                                    }
                                                }catch(Exception e){

                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {

                                            }
                                        }){
                                            @Override
                                            protected Map<String, String> getParams() throws AuthFailureError {
                                                HashMap<String,String> params = new HashMap<String,String>();
                                                SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

                                                try{
                                                    params.put("_id",Integer.toString(prefs.getInt("SongShareId",0)));
                                                    params.put("title", responseObj.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("title"));
                                                    params.put("artist", "");
                                                    params.put("art",responseObj.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"));
                                                    params.put("youtube_id",youtubeID);
                                                    params.put("username",prefs.getString("SongShareUser",""));
                                                }catch(Exception e){
                                                    Toast.makeText(getApplicationContext(),"Error sharing Youtube video...",Toast.LENGTH_SHORT).show();
                                                }

                                                return params;
                                            }
                                        };
                                        req.add(youtubeShare);
                                    }
                                });
                            }catch(Exception e){
                                Toast.makeText(getApplicationContext(),"Error getting Youtube video data...",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                    req.add(youtubeDataReq);
                }else{
                    musicData = GetSongAndArtist(songShareIntent.getStringExtra(Intent.EXTRA_TEXT));
                    SongTitleField.setText(musicData.getTitle());
                    ArtistField.setText(musicData.getArtist());
                    SendAPIRequest(musicData.getArtist(),musicData.getTitle());

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(getApplicationContext(),String.valueOf(data.getStringExtra(Intent.EXTRA_TEXT)),Toast.LENGTH_SHORT).show();

        if(requestCode == 1337){
            //We have made it through the Spotify authentication.
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode,data);
            final String accessToken = response.getAccessToken();

            if(response.getType() == AuthenticationResponse.Type.TOKEN){
                StringRequest SpotInfo = new StringRequest(Request.Method.GET, "https://api.spotify.com/v1/tracks/"+spotID, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.d("SPOTIFY INFO",response);
                            final JSONObject resultSong = ParseSpotifyInfo(new JSONObject(response));
                            SongTitleField.setText(resultSong.getString("title"));
                            ArtistField.setText(resultSong.getString("artist"));

                            AlbumArtLoader load = new AlbumArtLoader(albumImage,resultSong.getString("art"),imageSpinner);
                            load.execute("");

                            ShareBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    StringRequest ShareTrack = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/share/create", new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
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

                                            try{
                                                params.put("title", resultSong.getString("title"));
                                                params.put("artist", resultSong.getString("artist"));
                                                params.put("art",resultSong.getString("art"));
                                                params.put("spotify_id",resultSong.getString("uri"));
                                                params.put("username",prefs.getString("SongShareUser",""));
                                            }catch(Exception e){
                                                Toast.makeText(getApplicationContext(),"ERROR SHARING TRACK TO DATABASE",Toast.LENGTH_LONG).show();
                                            }

                                            return params;
                                        }
                                    };
                                    req.add(ShareTrack);
                                }
                            });
                        }catch(Exception e){
                            Toast.makeText(getApplicationContext(),"ERROR PULLING DATA FROM SPOTIFY",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();

                        params.put("Content-Type","application/json");
                        params.put("Authorization","Bearer "+accessToken);

                        return params;
                    }
                };
                req.add(SpotInfo);
            }else{
                Log.d("SPOT ERROR",response.getError().toString());
                Toast.makeText(getApplicationContext(),"ERROR AUTHENTICATING WITH SPOTIFY",Toast.LENGTH_LONG).show();
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
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=d416d5b04ba1b47bf4aad5f6123889ba&artist="+artist.replace(" ","%20")+"&track="+track.replace(" ","%20")+"&format=json";
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
                                    final JSONObject largeImage = covers.getJSONObject(2);
                                    musicData.setArt(largeImage.getString("#text"));
                                    AlbumArtLoader load = new AlbumArtLoader(albumImage,largeImage.getString("#text"),imageSpinner);
                                    load.execute("");

                                    ShareBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            StringRequest ShareTrack = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/share/create", new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {

                                                    try{
                                                        JSONObject obj = new JSONObject(response);

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
                                                    Map<String,String> params = new HashMap<String, String>();

                                                    try{
                                                        SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

                                                        params.put("_id",Integer.toString(prefs.getInt("SongShareId",0)));
                                                        params.put("title", SongTitleField.getText().toString());
                                                        params.put("artist", ArtistField.getText().toString());
                                                        params.put("art",largeImage.getString("#text"));
                                                        params.put("username",prefs.getString("SongShareUser",""));
                                                    }catch(Exception e){

                                                    }

                                                    return params;
                                                }
                                            };
                                            queue.add(ShareTrack);
                                        }
                                    });
                                }catch (JSONException e){
                                    Toast.makeText(getApplicationContext(),"Unable to find album art for track.",Toast.LENGTH_SHORT).show();
                                    imageSpinner.setVisibility(View.GONE);
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

    private JSONObject ParseSpotifyInfo(JSONObject data){
        JSONObject parsedObj = new JSONObject();

        try{
            parsedObj.put("title",data.getString("name"));
            parsedObj.put("artist",data.getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name"));
            parsedObj.put("art",data.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"));
            parsedObj.put("uri",data.getString("uri"));
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"ERROR PARSING SONG DATA",Toast.LENGTH_LONG).show();
        }

        return parsedObj;
    }

}
