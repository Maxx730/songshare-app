package com.squidswap.songshare.songshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

//Main Pager activity that serves as the main view of the application.
public class SongSharePagerMain extends FragmentActivity {

    private ViewPager pager;
    private SongSharePagerAdapter adapter;
    private LinearLayout SearchStaticLayout;
    private ImageView RefreshButton,ProfileButton,FabBtn;
    private SharedPreferences check;
    private LinearLayout SongShareFab;
    private RelativeLayout NotifLay;
    private boolean FAB_OPEN = false;
    private NotificationHandler notification;
    private RelativeLayout NotifBadge;
    private TextView NotifCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if the user is already logged in or not.
        check = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        if(!check.contains("SongShareUser") && !check.contains("SongSharePassword") && !check.contains("SongShareId")) {
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(i);
        }else{
            notification = new NotificationHandler(getApplicationContext(), String.valueOf(check.getInt("SongShareId", 0)), new SongshareNotificationInterface() {
                @Override
                public void NotificationRecieved() {
                    if(notification.GetNotifCount() > 0){
                        NotifBadge.setVisibility(View.VISIBLE);
                        NotifCount.setText(String.valueOf(notification.GetNotifCount()));
                    }
                }
            });

            CreateNotificationChannel();
            //Let the user know to swipe right or left to view friends and shares etc.
            Toast.makeText(getApplicationContext(),"Swipe Right to View Friends",Toast.LENGTH_SHORT).show();
            //Set the layout of the Fragment Activity to the view pager XML file.
            setContentView(R.layout.songshare_main_view);

            //Grab the search layout.
            SearchStaticLayout = findViewById(R.id.SearchTopStatic);

            //Grab the UI elements.
            pager = findViewById(R.id.SongSharePager);
            RefreshButton = findViewById(R.id.SearchRefresh);
            ProfileButton = findViewById(R.id.SearchProfile);
            adapter = new SongSharePagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            NotifLay = (RelativeLayout) findViewById(R.id.NotifLayout);
            NotifBadge = (RelativeLayout) findViewById(R.id.NotificationBadge);
            NotifCount = (TextView) findViewById(R.id.NotificationCount);

            ProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(),EditProfileOptions.class);
                    startActivity(i);
                }
            });

            RefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RefreshButton.animate().setDuration(700).rotation(720f).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            RefreshButton.setRotation(0f);
                            super.onAnimationEnd(animation);
                        }
                    }).start();
                }
            });

            NotifLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(),SongshareNotifications.class);
                    startActivity(i);
                }
            });

            //Show and hide certain UI elements based on the screen that has been scrolled to.
            pager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int page = pager.getCurrentItem();

                    if(page == 3){
                        SearchStaticLayout.setVisibility(View.GONE);
                    }else{
                        SearchStaticLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    //Create our notification channel
    private void CreateNotificationChannel(){
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/all");
        FirebaseMessaging.getInstance().subscribeToTopic("friend_requests_"+String.valueOf(check.getInt("SongShareId",0)));
        FirebaseMessaging.getInstance().subscribeToTopic("group_requests_"+String.valueOf(check.getInt("SongShareId",0)));

        //Check if we are in Oreo, if so we need to create a notification channel for
        //our notification.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "SongShareChannel";
            String description = "Songshare Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("com.squidswap.songshare.songshare",name,importance);
            channel.setDescription(description);
            NotificationManager notManage = getSystemService(NotificationManager.class);
            notManage.createNotificationChannel(channel);
        }
    }

    //Class used for handling the different fragment pages in the fragment activity pager.
    private class SongSharePagerAdapter extends FragmentStatePagerAdapter{

        public SongSharePagerAdapter(FragmentManager m){
            super(m);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment ac;

            switch(position){
                case 0:
                    ac = new SharesFragment();
                    break;
                case 1:
                    ac = new FriendsFragment();
                    break;
                case 2:
                    ac = new GroupsFragment();
                    break;
                case 3:
                    ac = new SettingsFragmentView();
                    break;
                default:
                    ac = new SharesFragment();
            }

            return ac;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
