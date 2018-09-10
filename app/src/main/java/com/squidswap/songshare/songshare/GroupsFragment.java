package com.squidswap.songshare.songshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsFragment extends Fragment {

    private RequestQueue req;
    private ImageButton CreateGroup,InvitesButton,CloseModal;
    private RelativeLayout InviteShade;
    private SharedPreferences prefs;
    private ListView InviteList;
    private GridView GroupGrid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.groups_view_fragment,container,false);

        //Grab our non UI elements
        req = Volley.newRequestQueue(getActivity().getApplicationContext());
        prefs = getActivity().getSharedPreferences("SongShareLogin",Context.MODE_PRIVATE);
        final int USER_ID = prefs.getInt("SongShareId",0);

        //Grab UI elements
        CreateGroup = rootView.findViewById(R.id.CreateGroupButton);
        InviteShade = rootView.findViewById(R.id.ShadeLayout);
        InvitesButton = rootView.findViewById(R.id.ShowInvitesButton);
        CloseModal = rootView.findViewById(R.id.CloseInviteModal);
        InviteList = rootView.findViewById(R.id.InviteList);
        GroupGrid = rootView.findViewById(R.id.GroupList);

        InvitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteShade.setVisibility(View.VISIBLE);
            }
        });

        CloseModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteShade.setVisibility(View.GONE);
            }
        });

        CreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity().getApplicationContext(),createGroupActivity.class);
                startActivity(i);
            }
        });

        StringRequest groupString = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/groups", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject rep = new JSONObject(response);
                    JSONArray groupList = rep.getJSONArray("PAYLOAD");
                    ArrayList<JSONObject> objs = new ArrayList<>();

                    for(int i = 0;i < groupList.length();i++){
                        objs.add(groupList.getJSONObject(i));
                    }

                    GroupAdapter grp = new GroupAdapter(getActivity().getApplicationContext(),0,objs,"groups");
                    GroupGrid.setAdapter(grp);
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

                params.put("user_id",String.valueOf(prefs.getInt("SongShareId",0)));

                return params;
            }
        };
        req.add(groupString);

        LoadInvites(USER_ID);

        return rootView;
    }

    private void LoadInvites(final int id){
        StringRequest reqInvites = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/group/invites", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject resp = new JSONObject(response);
                    JSONArray payload = resp.getJSONArray("PAYLOAD");
                    ArrayList<JSONObject> objs = new ArrayList<>();

                    for(int i = 0;i < payload.length();i++){
                        objs.add(payload.getJSONObject(i));
                    }

                    GroupAdapter adapt = new GroupAdapter(getActivity().getApplicationContext(),0,objs,"invites");
                    InviteList.setAdapter(adapt);
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

                params.put("reciever_id",String.valueOf(id));

                return params;
            }
        };
        req.add(reqInvites);
    }

    class GroupAdapter extends ArrayAdapter<JSONObject>{

        private Context con;
        private String type;
        private ArrayList<JSONObject> objs;

        public GroupAdapter(@NonNull Context context, int resource, @NonNull ArrayList<JSONObject> objects,String type) {
            super(context, resource, objects);

            this.con = context;
            this.type = type;
            this.objs = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = getLayoutInflater();

            if(this.type == "invites"){
                convertView = inflate.inflate(R.layout.single_group_item,parent,false);
                TextView inviteTitle = convertView.findViewById(R.id.InviteTitle);
                final Button acceptInvite = convertView.findViewById(R.id.InviteAccept);

                try{
                    inviteTitle.setText(this.objs.get(position).getString("title"));

                    acceptInvite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StringRequest acReq = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/group/accept", new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try{
                                       JSONObject chec = new JSONObject(response);

                                       if(chec.getString("TYPE").equals("SUCCESS")){
                                           objs.remove(position);
                                           notifyDataSetChanged();
                                       }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getActivity().getApplicationContext(),"Error accepting invite.",Toast.LENGTH_LONG).show();
                                }
                            }){
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    HashMap<String,String> params = new HashMap<>();

                                    try{
                                        params.put("acceptor_id",String.valueOf(prefs.getInt("SongShareId",0)));
                                        params.put("group_id",String.valueOf(objs.get(position).getInt("_id")));
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                    return params;
                                }
                            };
                            req.add(acReq);
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else if(this.type == "groups"){
                convertView = getLayoutInflater().inflate(R.layout.group_grid_item,parent,false);
                TextView TitleText = convertView.findViewById(R.id.GroupTitleText);
                ImageView GroupImage = convertView.findViewById(R.id.GroupGridImage);

                try{
                    FirebaseMessaging.getInstance().subscribeToTopic("group_share_"+String.valueOf(objs.get(position).getInt("_id")));
                    TitleText.setText(objs.get(position).getString("title"));

                    FirebaseMessaging.getInstance().subscribeToTopic("group_"+getItem(position).getString("_id"));

                    GroupImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity().getApplicationContext(),SingleGroup.class);
                            try{
                                i.putExtra("GroupId",objs.get(position).getString("_id"));
                                startActivity(i);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            return convertView;
        }
    }
}
