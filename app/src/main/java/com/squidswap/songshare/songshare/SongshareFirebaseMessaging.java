package com.squidswap.songshare.songshare;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class SongshareFirebaseMessaging extends FirebaseMessagingService {
    private SharedPreferences prefs;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "com.squidswap.songshare.songshare")
                    .setSmallIcon(R.drawable.fontawesome_start)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager manage = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manage.notify(1,mBuilder.build());


        super.onMessageReceived(remoteMessage);
    }
}
