package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
    private LinearLayout Friends,Settings,FriendListLayout,RequestListLayout;
    private RelativeLayout Shares,FindFriendsButton,ListFriendToggle,ListRequestToggle,ToggleFriendsIndi,ToggleReqIndi;
    private Switch ToggleNavPosition,ToggleOpenButtons;
    private SongShareListListener shareListen;

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
        shareList = findViewById(R.id.ShareList);
        Shares = findViewById(R.id.ShareLayout);
        Friends = findViewById(R.id.FriendsLayout);

    }

    private void HideFriendsIndis(){
        ToggleFriendsIndi.setBackgroundColor(getResources().getColor(R.color.slightGray));
        ToggleReqIndi.setBackgroundColor(getResources().getColor(R.color.slightGray));
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
                                LoadRequests();

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

}
