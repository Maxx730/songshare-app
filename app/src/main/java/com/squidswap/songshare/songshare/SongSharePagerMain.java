package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

//Main Pager activity that serves as the main view of the application.
public class SongSharePagerMain extends FragmentActivity {

    private ViewPager pager;
    private SongSharePagerAdapter adapter;
    private LinearLayout SearchStaticLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if the user is already logged in or not.
        SharedPreferences check = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        if(!check.contains("SongShareUser") && !check.contains("SongSharePassword") && !check.contains("SongShareId")) {
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(i);
        }else{
            //Let the user know to swipe right or left to view friends and shares etc.
            Toast.makeText(getApplicationContext(),"Swipe Right to View Friends",Toast.LENGTH_SHORT).show();
            //Set the layout of the Fragment Activity to the view pager XML file.
            setContentView(R.layout.songshare_main_view);

            //Grab the search layout.
            SearchStaticLayout = findViewById(R.id.SearchTopStatic);

            //Grab the UI elements.
            pager = findViewById(R.id.SongSharePager);
            adapter = new SongSharePagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);

            //Show and hide certain UI elements based on the screen that has been scrolled to.
            pager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    System.out.println("Slide: "+String.valueOf(pager.getCurrentItem()));

                    int page = pager.getCurrentItem();

                    if(page == 2){
                        SearchStaticLayout.setVisibility(View.GONE);
                    }else{
                        SearchStaticLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
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
                    ac = new SettingsFragmentView();
                    break;
                default:
                    ac = new SharesFragment();
            }

            return ac;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}