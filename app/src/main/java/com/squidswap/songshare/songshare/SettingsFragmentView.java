package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

public class SettingsFragmentView extends Fragment {
    private Button LogoutButton;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;
    private Switch InstructShow,SettingsIcon,RecieveNotifs,IncludeOwnShares;
    private LinearLayout ToFindFriends,LogoutLayout,EditProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.settings_view_fragment,container,false);

        //Grabbed our shared preferences object here.
        prefs = getActivity().getSharedPreferences("SongShareLogin",getActivity().MODE_PRIVATE);
        edit = prefs.edit();

        //Grab our UI elements here.
        LogoutButton = rootView.findViewById(R.id.LogoutButton);
        EditProfile = rootView.findViewById(R.id.EditProfileLayout);
        RecieveNotifs = rootView.findViewById(R.id.RecieveNotifSwitch);
        IncludeOwnShares = rootView.findViewById(R.id.IncludeOwnShares);
        LogoutLayout = rootView.findViewById(R.id.LogoutLayout);

        RecieveNotifs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(RecieveNotifs.isChecked()){
                    edit.putBoolean("RecieveNotifs",true);
                }else{
                    edit.putBoolean("RecieveNotifs",false);
                }
            }
        });

        IncludeOwnShares.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(IncludeOwnShares.isChecked()){
                    edit.putBoolean("IncludeOwnShares",true);
                }else{
                    edit.putBoolean("IncludeOwnShares",false);
                }
            }
        });

        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),EditProfileOptions.class);
                startActivity(i);
            }
        });

        //Set the UI click events here.
        LogoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.remove("SongShareUser");
                edit.remove("SongSharePassword");
                edit.remove("SongShareId");
                edit.commit();

                Intent i = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
                startActivity(i);
            }
        });

        return rootView;
    }
}
