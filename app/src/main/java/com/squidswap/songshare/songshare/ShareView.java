package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShareView extends AppCompatActivity {

    private RequestQueue req;
    private ListView userFriends,requestList,shareList;
    private Button LogoutButton;
    private ImageButton FriendToggle,SettingsToggle,ShareToggle;
    private LinearLayout Shares,Friends,Settings,FriendListLayout,RequestListLayout;
    private Button ListFriendToggle,ListRequestToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_share_view);

        //Check if the user is already logged in or not.
        SharedPreferences check = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        if(!check.contains("SongShareUser") && !check.contains("SongSharePassword") && !check.contains("SongShareId")) {
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(i);
        }

        req = Volley.newRequestQueue(getApplicationContext());
        userFriends = findViewById(R.id.FriendsList);
        LogoutButton = findViewById(R.id.LogoutButton);
        FriendToggle = findViewById(R.id.FriendsToggle);
        SettingsToggle = findViewById(R.id.SettingsToggle);
        ShareToggle = findViewById(R.id.SharesToggle);
        shareList = findViewById(R.id.ShareList);

        Shares = findViewById(R.id.ShareLayout);
        Friends = findViewById(R.id.FriendsLayout);
        Settings = findViewById(R.id.SettingsLayout);

        FriendListLayout = findViewById(R.id.FriendListLayout);
        RequestListLayout = findViewById(R.id.RequestLayout);
        ListFriendToggle = findViewById(R.id.ToggleFriends);
        ListRequestToggle = findViewById(R.id.ToggleRequests);
        requestList = findViewById(R.id.RequestList);

        ListFriendToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestListLayout.setVisibility(View.GONE);
                FriendListLayout.setVisibility(View.VISIBLE);
                LoadFriends();
            }
        });

        ListRequestToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendListLayout.setVisibility(View.GONE);
                RequestListLayout.setVisibility(View.VISIBLE);
                LoadRequests();
            }
        });

        SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        FriendToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideLayouts();
                Friends.setVisibility(View.VISIBLE);
                LoadFriends();
            }
        });

        SettingsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideLayouts();
                Settings.setVisibility(View.VISIBLE);
            }
        });

        ShareToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideLayouts();
                Shares.setVisibility(View.VISIBLE);
            }
        });

        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove("SongShareUser");
                edit.remove("SongSharePassword");
                edit.remove("_id");
                edit.commit();
                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
            }
        });

        LoadFriends();
        LoadRequests();
        LoadShares();
    }

    private void LoadRequests(){
        SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
        StringRequest getRequests = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+String.valueOf(prefs.getInt("SongShareId",0))+"/requests", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    System.out.println(response);
                    JSONObject rep = new JSONObject(response);
                    JSONArray friends = rep.optJSONArray("PAYLOAD");
                    ArrayList<JSONObject> objs = new ArrayList<JSONObject>();

                    for(int i = 0;i < friends.length();i++) {
                        objs.add(friends.getJSONObject(i));
                    }

                    RequestListAdapter list = new RequestListAdapter(getApplicationContext(),R.layout.singe_request_item,objs);
                    requestList.setAdapter(list);
                }catch(Exception e){
                    System.out.println("ERROR BUILDING FRIENDS LIST");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(getRequests);
    }

    private void LoadFriends(){
        SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
        StringRequest getFriends = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+String.valueOf(prefs.getInt("SongShareId",0))+"/friends", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    System.out.println(response);
                    JSONObject rep = new JSONObject(response);
                    JSONArray friends = rep.optJSONArray("PAYLOAD");
                    ArrayList<JSONObject> objs = new ArrayList<JSONObject>();

                    for(int i = 0;i < friends.length();i++) {
                        objs.add(friends.getJSONObject(i));
                    }

                    FriendListAdapter friendList = new FriendListAdapter(getApplicationContext(),R.layout.single_friend_item,objs);
                    userFriends.setAdapter(friendList);
                }catch(Exception e){
                    System.out.println("ERROR BUILDING FRIENDS LIST");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(getFriends);
    }

    private void LoadShares(){
        SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
        StringRequest getShares = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/" + String.valueOf(prefs.getInt("SongShareId", 0)) + "/shares", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject mainObj = new JSONObject(response);
                    ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
                    JSONArray sour = mainObj.getJSONArray("PAYLOAD");

                    for(int i = 0;i < sour.length();i++){
                        objs.add(sour.getJSONObject(i));
                    }

                    ShareAdapter shareAdapt = new ShareAdapter(getApplicationContext(),R.layout.single_share_item,objs);
                    shareList.setAdapter(shareAdapt);
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"ERROR PULLING SHARE DATA",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(getShares);
    }


    private void HideLayouts(){
        Shares.setVisibility(View.GONE);
        Settings.setVisibility(View.GONE);
        Friends.setVisibility(View.GONE);
    }


    class FriendListAdapter extends ArrayAdapter<JSONObject>{

        public FriendListAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = LayoutInflater.from(getApplicationContext());
            convertView = inflate.inflate(R.layout.single_friend_item,parent,false);

            TextView username = convertView.findViewById(R.id.FriendUsername);
            TextView status = convertView.findViewById(R.id.FriendStatus);

            try{
                username.setText(getItem(position).getString("username"));
            }catch(Exception e){
                System.out.println("ERROR BUILDING FRIEND LIST ITEM");
            }

            return convertView;
        }
    }

    class RequestListAdapter extends ArrayAdapter<JSONObject>{

        public RequestListAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = LayoutInflater.from(getApplicationContext());
            convertView = inflate.inflate(R.layout.singe_request_item,parent,false);

            System.out.println(getItem(position).toString());

            TextView requestUser = convertView.findViewById(R.id.RequestUser);
            Button AcceptOffer = convertView.findViewById(R.id.AcceptButton);

            try{
                requestUser.setText(getItem(position).getString("username"));

                AcceptOffer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StringRequest AcceptFriend = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/friend/accept", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println(response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }){
                            @Override
                            protected Map<String,String> getParams(){
                                SharedPreferences p = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
                                Map<String,String> params = new HashMap<String, String>();

                                try{
                                    params.put("acceptedId",String.valueOf(getItem(position).getInt("request_id")));
                                    params.put("user_id",String.valueOf(p.getInt("SongShareId",0)));
                                    params.put("friend_id",String.valueOf(getItem(position).getInt("_id")));
                                }catch(Exception e){

                                }

                                return params;
                            }
                        };
                        req.add(AcceptFriend);
                    }
                });
            }catch(Exception e){
                System.out.println("ERROR BUILDING FRIEND LIST ITEM");
            }

            return convertView;
        }
    }

    class ShareAdapter extends ArrayAdapter<JSONObject>{

        private Context con;
        private int layout;
        private ArrayList<JSONObject> shares;

        public ShareAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> shares) {
            super(context, resource,shares);

            this.con = context;
            this.layout = resource;
            this.shares = shares;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = getLayoutInflater().from(getApplicationContext());
            convertView = inflate.inflate(R.layout.single_share_item,parent,false);
            TextView singleShareTitle = convertView.findViewById(R.id.SingleTrackTitle);
            TextView singleShareArtist = convertView.findViewById(R.id.SingleTrackArtist);
            ImageView sharedArtwork = convertView.findViewById(R.id.SharedArtwork);

            try{
                singleShareTitle.setText(getItem(position).getString("title"));
                singleShareArtist.setText(getItem(position).getString("artist"));
                Glide.with(convertView).load(getItem(position).getString("art")).into(sharedArtwork);
            }catch(Exception e){

            }

            return convertView;
        }
    }
}
