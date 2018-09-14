package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class SongshareNotifications extends AppCompatActivity {

    private NotificationHandler notifications;
    private SharedPreferences prefs;
    private ListView NotifList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_songshare_notifications);

        //Grab UI elements
        NotifList = (ListView) findViewById(R.id.NotificationList);

        //Grab non UI elements
        prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        notifications = new NotificationHandler(getApplicationContext(), String.valueOf(prefs.getInt("SongShareId", 0)), new SongshareNotificationInterface() {
            @Override
            public void NotificationRecieved() {
                NotifList.setAdapter(new NotificationAdapter(getApplicationContext(),notifications.GetNotifs()));
            }
        });
    }

    private class NotificationAdapter extends ArrayAdapter{

        private Context con;
        private ArrayList<Notification> objects;

        public NotificationAdapter(@NonNull Context context, @NonNull ArrayList<Notification> objs) {
            super(context, 0, objs);

            this.con = context;
            this.objects = objs;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if(this.objects.get(position).getType() == "friend"){
                convertView = getLayoutInflater().inflate(R.layout.friend_notificaiton,parent,false);
                final View final_view = convertView;

                final Button AcceptBtn = convertView.findViewById(R.id.FriendAcceptBtn);
                TextView UserFriendName = convertView.findViewById(R.id.FriendRequestUser);

                UserFriendName.setText(this.objects.get(position).getName() + " would like to be your friend!");

                AcceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        objects.get(position).AcceptNotification(getApplicationContext(), new SongshareNotificationInterface() {
                            @Override
                            public void NotificationRecieved() {
                                AcceptBtn.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }else{
                convertView = getLayoutInflater().inflate(R.layout.group_notification,parent,false);

                TextView GroupText = convertView.findViewById(R.id.GroupUserName);

                GroupText.setText("You have been invited to join "+this.objects.get(position).getName());
            }

            return convertView;
        }
    }
}
