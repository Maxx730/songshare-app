package com.squidswap.songshare.songshare;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class SingleShare extends AppCompatActivity {

    private RequestQueue req;
    private TextView SharerText,TitleText,ArtistText,AlbumText,BackToMain;
    private ImageView albumImage;
    private Intent data;
    private ProgressBar singleShareLoading;
    private RelativeLayout singeShareImage;
    private Button openSpotify,openGooglePlay;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_single_share);

        TitleText = findViewById(R.id.SharedSingleTitle);
        ArtistText = findViewById(R.id.SharedSingleArtist);
        albumImage = findViewById(R.id.AlbumImage);
        singleShareLoading = findViewById(R.id.LoadingSingleShare);
        singeShareImage = findViewById(R.id.SingleShareImageLayout);
        openSpotify = findViewById(R.id.OpenInSpotify);
        openGooglePlay = findViewById(R.id.OpenInGooglePlay);
        BackToMain = findViewById(R.id.BackToMain);
        pm = getPackageManager();

        data = getIntent();

        BackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),SongSharePagerMain.class);
                startActivity(i);
            }
        });

        req = Volley.newRequestQueue(getApplicationContext());
        StringRequest ShareRequest = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/share/"+data.getIntExtra("TrackID",1), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    System.out.println(response);
                    JSONObject res = new JSONObject(response);
                    final JSONObject payload = res.getJSONArray("PAYLOAD").getJSONObject(0);

                    TitleText.setText(payload.getString("title"));
                    ArtistText.setText(payload.getString("artist"));
                    Glide.with(getApplicationContext()).load(payload.getString("art")).into(albumImage);

                    albumImage.animate().scaleY(1).scaleX(1).setDuration(500).start();

                    singleShareLoading.setVisibility(View.GONE);
                    singeShareImage.setVisibility(View.VISIBLE);

                    openSpotify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                pm.getPackageInfo("com.spotify.music",0);
                                Intent i = new Intent(Intent.ACTION_VIEW);

                                if(payload.getString("spot_uri") != ""){
                                    i.setData(Uri.parse(payload.getString("spot_uri")));
                                    i.putExtra(Intent.EXTRA_REFERRER,Uri.parse("android-app://" + getApplicationContext().getPackageName()));
                                    startActivity(i);
                                }
                            }catch(Exception e){
                                Toast.makeText(getApplicationContext(),"Spotify must be installed to play track!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    openGooglePlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try{
                                String title = payload.getString("title");
                                String artist = payload.getString("artist");

                                Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
                                        MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
                                intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE,title);
                                intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST,artist);
                                intent.putExtra(SearchManager.QUERY,title + " " + artist);
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            }catch(Exception e){

                            }
                        }
                    });
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Error parsing track data...",Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(ShareRequest);
    }
}

