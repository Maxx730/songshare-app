package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SingleGroup extends AppCompatActivity {

    private GroupPagerAdapter adapter;
    private ViewPager pager;
    private RequestQueue req;
    private TextView GroupTitle,GroupDesc;
    private String GROUP_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_single_group);

        //Grab non UI elements.
        req = Volley.newRequestQueue(getApplicationContext());

        LoadGroupInfo();

        //Grab UI elements
        GroupTitle = findViewById(R.id.SingleGroupTitle);
        GroupDesc = findViewById(R.id.SingleGroupDescription);
        pager = findViewById(R.id.GroupPager);
        adapter = new GroupPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        //Grab the ID for the chosen group
        Intent i = getIntent();
        GROUP_ID = i.getStringExtra("GroupId");
    }

    private void LoadGroupInfo(){
        StringRequest GroupInfo = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/group", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject resp = new JSONObject(response).getJSONArray("PAYLOAD").getJSONObject(0);

                    GroupTitle.setText(resp.getString("title"));
                    GroupDesc.setText(resp.getString("description"));
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

                params.put("group_id",String.valueOf(GROUP_ID));

                return params;
            }
        };
        req.add(GroupInfo);
    }

    private class GroupPagerAdapter extends FragmentStatePagerAdapter{

        public GroupPagerAdapter(FragmentManager m){
            super(m);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment ac;

            switch(position){
                case 0:
                    ac = new SharesFragment();
                    Bundle shareType = new Bundle();
                    shareType.putString("type","group");
                    shareType.putString("group_id",GROUP_ID);
                    ac.setArguments(shareType);
                    break;
                default:
                    ac = new SharesFragment();
                    break;
            }

            return ac;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}
