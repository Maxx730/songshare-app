package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class GroupsFragment extends Fragment {

    private RequestQueue req;
    private ImageButton CreateGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.groups_view_fragment,container,false);

        //Grab our non UI elements
        req = Volley.newRequestQueue(getActivity().getApplicationContext());

        //Grab UI elements
        CreateGroup = rootView.findViewById(R.id.CreateGroupButton);

        CreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity().getApplicationContext(),createGroupActivity.class);
                startActivity(i);
            }
        });

        StringRequest groupString = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/groups", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(groupString);

        return rootView;
    }

    class GroupAdapter extends ArrayAdapter<JSONObject>{

        public GroupAdapter(@NonNull Context context, int resource, @NonNull JSONObject[] objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = getLayoutInflater();

            convertView = inflate.inflate(R.layout.single_group_item,parent,false);
            return convertView;
        }
    }
}
