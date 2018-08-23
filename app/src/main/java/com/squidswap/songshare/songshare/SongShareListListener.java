package com.squidswap.songshare.songshare;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SongShareListListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("Touching!","ahh yess");
        return false;
    }
}
