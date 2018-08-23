package com.squidswap.songshare.songshare;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class SettingsScreen extends AppCompatActivity {

    private Button LogoutButton;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        //Get our shared preferences object.
        prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
        edit = prefs.edit();

        //Grab our UI elements here and set click listeners.

    }
}
