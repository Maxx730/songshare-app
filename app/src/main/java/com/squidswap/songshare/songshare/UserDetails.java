package com.squidswap.songshare.songshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class UserDetails extends AppCompatActivity {

    private ImageView UserImage,ChevronUp;
    private Button EditButton,CancelButton,SaveButton;
    private ImageButton FriendButton;
    private TextView UserNamePlace,ExpandText;
    private SharedPreferences prefs;
    private RequestQueue req;
    private LinearLayout bottomLayout;
    private UserDetailsBottomListener botListen;
    private GestureDetector gest;
    private RelativeLayout ExitDetails;
    private int USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_user_details);

        final Intent i = getIntent();
        User data = new User(i.getIntExtra("userId",0));

        prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
        botListen = new UserDetailsBottomListener();
        gest = new GestureDetector(this,botListen);
        req = Volley.newRequestQueue(getApplicationContext());

        //Grab our UI elements here.
        UserNamePlace = findViewById(R.id.UserDetailName);
        UserImage = findViewById(R.id.DetailsUserImage);
        ExitDetails = findViewById(R.id.ExitUserDetails);

        ExitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Set the user id for pulling user info.
        USER_ID = i.getIntExtra("userId",0);

        if(i.getIntExtra("userId",0) == prefs.getInt("SongShareId",0)){
            Toast.makeText(getApplicationContext(),"User can be edited.",Toast.LENGTH_SHORT).show();
            EditButton.setVisibility(View.VISIBLE);
        }else{
            data.CheckFriends(i.getIntExtra("userId",0),prefs.getInt("SongShareId",0));
        }
    }

    private class User{
        private String username,email;
        private int user_id;
        private RequestQueue req;

        public User(int id){
            if(id > 0){
                this.user_id = id;
                this.req = Volley.newRequestQueue(getApplicationContext());

                this.LoadData();
            }else{
                Toast.makeText(getApplicationContext(),"Error pulling user data...",Toast.LENGTH_SHORT).show();
            }
        }

        private void LoadData(){
            StringRequest userInfoReq = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+this.user_id, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONArray repObj = new JSONArray(response);
                        UserNamePlace.setText(repObj.getJSONObject(0).getString("username"));
                        Glide.with(getApplicationContext()).load(repObj.getJSONObject(0).getString("profile")).into(UserImage);
                    }catch(Exception e){

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            this.req.add(userInfoReq);

            StringRequest loadGroups = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/groups", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject rep = new JSONObject(response);
                        ArrayList<JSONObject> objs = new ArrayList<>();

                        for(int i = 0;i < rep.getJSONArray("PAYLOAD").length();i++){
                            objs.add(rep.getJSONArray("PAYLOAD").getJSONObject(i));
                        }

                        SongShareRecyclerViewAdapter adapt = new SongShareRecyclerViewAdapter(getApplicationContext(),objs);
                        RecyclerView rec = findViewById(R.id.GroupMemberList);

                        //Set up the groups recycler view.
                        rec.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
                        rec.setAdapter(adapt);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> params = new HashMap<>();

                    params.put("user_id", String.valueOf(USER_ID));

                    return params;
                }
            };
            req.add(loadGroups);
        }

        private void CheckFriends(final int friend,final int user){
            StringRequest checkReq = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/friends/check", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("CHECK VALUE",response);
                    try{
                        JSONObject check = new JSONObject(response);

                        if(check.getString("TYPE").equals("TRUE")){
                            FriendButton.setVisibility(View.GONE);
                        }
                    }catch(Exception e){

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> params = new HashMap<String,String>();
                    params.put("friend_id",String.valueOf(friend));
                    params.put("user_id",String.valueOf(user));
                    return params;
                }
            };
            this.req.add(checkReq);
        }
    }

    //Adapter used for showing the users groups they are a part of.
    private class MemberAdapter extends ArrayAdapter{
        private Context con;
        private ArrayList<JSONObject> objs;

        public MemberAdapter(@NonNull Context context, @NonNull ArrayList<JSONObject> objs) {
            super(context, 0, objs);

            this.con = context;
            this.objs = objs;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }
}
