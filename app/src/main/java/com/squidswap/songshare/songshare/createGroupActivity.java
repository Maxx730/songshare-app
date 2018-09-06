package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class createGroupActivity extends AppCompatActivity {

    private RequestQueue req;
    private SharedPreferences prefs;
    private Button CancelCreation,CreateNextButton;
    private EditText GroupTitle,GroupDescrip;
    private ListView ChoiceList;
    private ArrayList<ChosenUser> RetrievedUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_group);

        //Initialize non UI elements
        req = Volley.newRequestQueue(getApplicationContext());
        prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        final int UserID = prefs.getInt("SongShareId",0);

        //Grab our UI elements
        CancelCreation = findViewById(R.id.CancelCreateGroup);
        CreateNextButton = findViewById(R.id.CreateGroupNext);
        GroupTitle = findViewById(R.id.GroupTitle);
        ChoiceList = findViewById(R.id.SelectUsersList);
        GroupDescrip = findViewById(R.id.GroupDescription);
        RetrievedUsers = new ArrayList<>();

        LoadFriends(req);

        GroupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(GroupTitle.getText().toString() != "" && GroupTitle.getText().length() > 0){
                    CreateNextButton.setEnabled(true);
                }else{
                    CreateNextButton.setEnabled(false);
                }
            }
        });

        CreateNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest createGroup = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/group/create", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject obj = new JSONObject(response);

                            if(obj.getString("TYPE").equals("SUCCESS")){
                                Toast.makeText(getApplicationContext(),"Group Created!",Toast.LENGTH_SHORT).show();
                                finish();
                            }else{

                            }
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
                        ArrayList<Integer> ids = new ArrayList<>();

                        for(int i = 0;i < RetrievedUsers.size();i++){
                            if(RetrievedUsers.get(i).GetStatus()){
                                ids.add(RetrievedUsers.get(i).UserId());
                            }
                        }

                        params.put("creator",String.valueOf(UserID));
                        params.put("title",GroupTitle.getText().toString());
                        params.put("description",GroupDescrip.getText().toString());
                        params.put("users",ids.toString());

                        return params;
                    }
                };
                req.add(createGroup);
            }
        });

        CancelCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog checkCancel = new AlertDialog.Builder(createGroupActivity.this).create();
                checkCancel.setTitle("Are you sure you want to cancel creating group?");
                checkCancel.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                checkCancel.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkCancel.hide();
                    }
                });

                checkCancel.show();
            }
        });
    }

    private void LoadFriends(RequestQueue req){
        StringRequest loadFriends = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+String.valueOf(prefs.getInt("SongShareId",0))+"/friends", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject rep = new JSONObject(response);
                    JSONArray ar = rep.getJSONArray("PAYLOAD");

                    for(int i = 0;i < ar.length();i++){
                        RetrievedUsers.add(new ChosenUser(ar.getJSONObject(i)));
                    }

                    ChoiceList.setAdapter(new AvailableFriends(getApplicationContext(),RetrievedUsers));
                }catch(Exception e){

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(loadFriends);
    }

    private class AvailableFriends extends ArrayAdapter<ChosenUser>{

        private Context con;
        private ArrayList<ChosenUser> friends;

        public AvailableFriends(Context context,ArrayList<ChosenUser> objs){
            super(context,0,objs);

            this.con = context;
            this.friends = objs;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.single_user_spin,parent,false);
            TextView username = convertView.findViewById(R.id.SpinUsername);
            ImageView profileImage = convertView.findViewById(R.id.UserSpinProfile);
            CheckBox chooseUser = convertView.findViewById(R.id.ChooseUserCheck);

            if(friends.get(position).isChecked){
                chooseUser.setChecked(true);
            }

            chooseUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    friends.get(position).SetChecked(isChecked);
                }
            });

            try{
                username.setText(this.friends.get(position).GetUsername());
                Glide.with(getApplicationContext()).load(this.friends.get(position).GetProfile()).into(profileImage);
            }catch(Exception e){
                e.printStackTrace();
            }

            return convertView;
        }
    }

    private class ChosenUser{
        private boolean isChecked;
        private JSONObject data;

        public ChosenUser(JSONObject data){
            isChecked = false;
            this.data = data;
        }

        public int UserId(){
            int value = 0;

            try{
                value = data.getInt("_id");
            }catch(Exception e){
                e.printStackTrace();
            }

            return value;
        }

        public String GetUsername(){
            String value = "";

            try{
                value = data.getString("username");
            }catch(Exception e){
                e.printStackTrace();
            }

            return value;
        }

        public String GetProfile(){
            String value = "";

            try{
                value = data.getString("profile");
            }catch(Exception e){
                e.printStackTrace();
            }

            return value;
        }

        public void SetChecked(boolean val){
            this.isChecked = val;
        }

        public boolean GetStatus(){
            return this.isChecked;
        }
    }
}
