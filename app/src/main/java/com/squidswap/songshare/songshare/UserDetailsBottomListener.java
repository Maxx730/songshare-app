package com.squidswap.songshare.songshare;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

//Class we are going to use to detect touch events on the details bottom layout.
public class UserDetailsBottomListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("GESTURE","DOWN");
        return super.onDown(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("GESTURE","MOVING");
        return super.onScroll(e1, e2, distanceX, distanceY);
    }
}
