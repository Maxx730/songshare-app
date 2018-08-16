package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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
    private TextView SharerText,TitleText,ArtistText,AlbumText;
    private ImageView albumImage;
    private Intent data;
    private ProgressBar singleShareLoading;
    private RelativeLayout singeShareImage;
    private ScrollView singleShareScroll;

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
        singleShareScroll = findViewById(R.id.SingleShareScrollLayout);

        data = getIntent();

        req = Volley.newRequestQueue(getApplicationContext());
        StringRequest ShareRequest = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/share/"+data.getIntExtra("TrackID",1), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject res = new JSONObject(response);
                    JSONObject payload = res.getJSONArray("PAYLOAD").getJSONObject(0);

                    TitleText.setText(payload.getString("title"));
                    ArtistText.setText(payload.getString("artist"));
                    Glide.with(getApplicationContext()).load(payload.getString("art")).into(albumImage);

                    singleShareLoading.setVisibility(View.GONE);
                    singleShareScroll.setVisibility(View.VISIBLE);
                    singeShareImage.setVisibility(View.VISIBLE);
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

