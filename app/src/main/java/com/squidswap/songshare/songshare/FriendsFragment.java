package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

//FRAGMENT LAYOUT FOR FRIENDS THAT WILL BE INSIDE THE VIEW PAGER OBJECT.
public class FriendsFragment extends Fragment {

    private GridView FriendsList;
    private RequestQueue req;
    private TextView friendsText,swipeRight;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.friends_view_fragment,container,false);

        FriendsList = rootView.findViewById(R.id.UserGridView);
        req = Volley.newRequestQueue(getActivity().getApplicationContext());
        friendsText = rootView.findViewById(R.id.NoFriendsText);
        swipeRight = rootView.findViewById(R.id.NoFriendsSwipe);
        LoadFriends();

        return rootView;
    }

    private void LoadFriends(){
        SharedPreferences prefs = getActivity().getSharedPreferences("SongShareLogin",getActivity().MODE_PRIVATE);
        StringRequest getFriends = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+String.valueOf(prefs.getInt("SongShareId",0))+"/friends", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject rep = new JSONObject(response);
                    JSONArray friends = rep.optJSONArray("PAYLOAD");
                    ArrayList<JSONObject> objs = new ArrayList<JSONObject>();

                    for(int i = 0;i < friends.length();i++) {
                        objs.add(friends.getJSONObject(i));
                    }

                    if(objs.size() > 0){
                         friendsText.setVisibility(View.GONE);
                         swipeRight.setVisibility(View.GONE);
                    }

                    FriendListAdapter friendList = new FriendListAdapter(getActivity().getApplicationContext(),R.layout.single_friend_item,objs);
                    FriendsList.setAdapter(friendList);
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

    //List adapter to display the users friends.
    class FriendListAdapter extends ArrayAdapter<JSONObject> {

        public FriendListAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            try{
                FirebaseMessaging.getInstance().subscribeToTopic("stream_"+getItem(position).getString("username"));
                Log.d("Subscripted To: ",getItem(position).getString("username"));
            }catch(Exception e){
                e.printStackTrace();
                Log.d("Failed To","damn");
            }

            LayoutInflater inflate = LayoutInflater.from(getActivity().getApplicationContext());
            convertView = inflate.inflate(R.layout.single_friend_item,parent,false);

            TextView username = convertView.findViewById(R.id.FriendUsername);
            final ImageView friendImage = convertView.findViewById(R.id.FriendImage);
            final ProgressBar prog = convertView.findViewById(R.id.FriendLoadingAnim);

            try{
                Log.d("DATA",getItem(position).toString());
                username.setText(getItem(position).getString("username"));

                Glide.with(getActivity().getApplicationContext()).load(getItem(position).getString("profile")).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        prog.setVisibility(View.GONE);
                        friendImage.setClipToOutline(true);
                        return false;
                    }
                }).into(friendImage);
            }catch(Exception e){
                System.out.println("ERROR BUILDING FRIEND LIST ITEM");
            }

            return convertView;
        }
    }
}
