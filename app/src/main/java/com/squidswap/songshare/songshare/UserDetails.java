package com.squidswap.songshare.songshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.LayoutAnimationController;
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
        FriendButton = findViewById(R.id.SendRequestButton);
        SaveButton = findViewById(R.id.SaveUserButton);
        EditButton = findViewById(R.id.EditUserButton);
        CancelButton = findViewById(R.id.CancelEditButton);
        UserImage = findViewById(R.id.DetailsUserImage);
        bottomLayout = findViewById(R.id.BottomInfoLayout);
        ExpandText = findViewById(R.id.ExpandText);
        ChevronUp = findViewById(R.id.ChevronUp);

        ExpandText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) bottomLayout.getLayoutParams();
                ValueAnimator val = ValueAnimator.ofInt(100,400).setDuration(500);

                val.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        par.height = Math.round((int) animation.getAnimatedValue() * getApplicationContext().getResources().getDisplayMetrics().density);
                        bottomLayout.setLayoutParams(par);
                    }
                });

                val.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Blurry.with(getApplicationContext()).radius(30).sampling(1).onto((RelativeLayout) findViewById(R.id.BlurView));
                        Blurry.with(getApplicationContext()).capture(UserImage).into(UserImage);
                    }
                });

                val.start();

                ChevronUp.animate().rotation(180).start();
            }
        });

        if(i.getIntExtra("userId",0) == prefs.getInt("SongShareId",0)){
            Toast.makeText(getApplicationContext(),"User can be edited.",Toast.LENGTH_SHORT).show();
            EditButton.setVisibility(View.VISIBLE);
        }else{
            FriendButton.setVisibility(View.VISIBLE);

            FriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StringRequest sendFriendReq = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/friend/add", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("RESPONSE",response);
                            FriendButton.setVisibility(View.GONE);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String,String> params = new HashMap<>();
                            params.put("sender",String.valueOf(prefs.getInt("SongShareId",0)));
                            params.put("reciever",String.valueOf(i.getIntExtra("userId",0)));
                            return params;
                        }
                    };
                    req.add(sendFriendReq);
                }
            });

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
                    Log.d("USER DATA",response);
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
}
