package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StreamView extends AppCompatActivity {

    private LinearLayout searchTabs;
    private EditText searchText;
    private RequestQueue searchQueue;
    private String searchTerm;
    private ListView userList;
    private FoundUserAdapter foundUsers;
    private JSONArray searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_stream_view);

        searchText = findViewById(R.id.SearchText);
        userList = findViewById(R.id.UserListView);

        searchQueue = Volley.newRequestQueue(getApplicationContext());

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTerm = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                final StringRequest searchReq = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/users/find", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            searchResults = new JSONArray(response);
                            ArrayList<JSONObject> objs = new ArrayList<JSONObject>();

                            for(int i = 0;i < searchResults.length();i++){
                                objs.add(searchResults.getJSONObject(i));
                            }

                            foundUsers = new FoundUserAdapter(getApplicationContext(),R.layout.found_user_item,objs);
                            userList.setAdapter(foundUsers);
                        }catch(Exception e){
                            System.out.println("ERROR BUILDING JSON ARRAY FOR RESULTS");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("searchTerm",searchTerm);
                        return params;
                    }
                };
                searchQueue.add(searchReq);
            }
        });
    }

    class FoundUserAdapter extends ArrayAdapter<JSONObject>{

        public FoundUserAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = LayoutInflater.from(getApplicationContext());
            convertView = inflate.inflate(R.layout.found_user_item,parent,false);
            TextView nameView = convertView.findViewById(R.id.UsernameView);


            return convertView;
        }
    }
}
