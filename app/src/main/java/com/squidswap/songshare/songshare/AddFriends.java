package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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

public class AddFriends extends AppCompatActivity {

    private Spinner userSpinner;
    private RequestQueue req;
    private SharedPreferences prefs;
    private ArrayList<JSONObject> recievedObjs,selectedObjs;
    private ImageButton AddUserButton;
    private SelectedAdapter selList;
    private ListView SelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add_friends);

        //Grab our UI elements
        userSpinner = findViewById(R.id.AddUserSpinner);
        AddUserButton = findViewById(R.id.AddSelectedUser);
        SelectedList = findViewById(R.id.SelectedUsers);

        //Initialize non UI elements
        req = Volley.newRequestQueue(getApplicationContext());
        prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
        recievedObjs = new ArrayList<>();
        selectedObjs = new ArrayList<>();

        AddUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedObjs.add(recievedObjs.get(userSpinner.getSelectedItemPosition()));
                recievedObjs.remove(userSpinner.getSelectedItemPosition());

                AddUserAdapter adapt = new AddUserAdapter(getApplicationContext(),recievedObjs);
                userSpinner.setAdapter(adapt);
                selList = new SelectedAdapter(getApplicationContext(),selectedObjs);
                SelectedList.setAdapter(selList);
            }
        });

        //Send request to server and grab all the users friends.
        StringRequest getFriends = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+String.valueOf(prefs.getInt("SongShareId",0))+"/friends", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject rep = new JSONObject(response);
                    JSONArray usrs = rep.getJSONArray("PAYLOAD");
                    ArrayList<JSONObject> userObjs = new ArrayList<>();

                    for(int i = 0;i < usrs.length();i++){
                        userObjs.add(usrs.getJSONObject(i));
                        recievedObjs.add(usrs.getJSONObject(i));
                    }

                    AddUserAdapter adapt = new AddUserAdapter(getApplicationContext(),userObjs);
                    userSpinner.setAdapter(adapt);

                    userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }catch(Exception e){
                    System.out.println("ERROR PARSING USER PLAYLOAD");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(getFriends);
    }

    //Class used for spinner array adapter.
    private class AddUserAdapter extends BaseAdapter {
        private Context con;
        private ArrayList<JSONObject> objs;

        public AddUserAdapter(Context context,ArrayList<JSONObject> objs){
            this.con = context;
            this.objs = objs;
        }

        @Override
        public int getCount() {
            return this.objs.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return this.objs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.single_user_spin,parent,false);
            TextView username = convertView.findViewById(R.id.SpinUsername);
            ImageView userImage = convertView.findViewById(R.id.UserSpinProfile);

            try{
                username.setText(getItem(position).getString("username"));

                Glide.with(getApplicationContext()).load(getItem(position).getString("profile")).into(userImage);
            }catch(Exception e){

            }

            return convertView;
        }
    }

    private class SelectedAdapter extends ArrayAdapter<JSONObject>{

        private Context con;
        private ArrayList<JSONObject> objs;

        public SelectedAdapter(@NonNull Context context, @NonNull ArrayList<JSONObject> objects) {
            super(context,0, objects);

            this.con = context;
            this.objs = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = getLayoutInflater();

            convertView = inflate.inflate(R.layout.added_freind_item,parent,false);
            TextView usernameText = convertView.findViewById(R.id.AddedFriendUsername);

            try{
                usernameText.setText(objs.get(position).getString("username"));
            }catch(Exception e){

            }

            return convertView;
        }
    }
}
