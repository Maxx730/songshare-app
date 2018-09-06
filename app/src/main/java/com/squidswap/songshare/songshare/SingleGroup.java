package com.squidswap.songshare.songshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class SingleGroup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_single_group);
    }
}
