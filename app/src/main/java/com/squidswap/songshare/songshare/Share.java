package com.squidswap.songshare.songshare;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Class for each single share item.
public class Share {
    private int id;
    private String title,artist,art;
    private User user;
    private RequestQueue req;
    private Context con;

    public Share(int id,String title,String artist,String art,User user,Context con){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.user = user;
        this.art = art;
        this.con = con;
        this.req = Volley.newRequestQueue(con);
    }

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){
        return this.artist;
    }

    public User getUser(){
        return this.user;
    }

    public String getArt(){
        return this.art;
    }

    //Functions for getting info from the server about the share.
    public void GetComments(final SongshareArrayInterface inter){
        final int id = this.id;

        StringRequest getComments = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/share/comments", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject obj = new JSONObject(response);
                    ArrayList<JSONObject> objs = new ArrayList<>();

                    for(int i = 0;i < obj.getJSONArray("PAYLOAD").length();i++){
                        objs.add(obj.getJSONArray("PAYLOAD").getJSONObject(i));
                    }

                    inter.AfterRetrieveArray(objs);
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

                params.put("shared",String.valueOf(id));

                return params;
            }
        };

        req.add(getComments);
    }

    public void AddComment(final String message,final int commentor){
        StringRequest AddComment = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/share/add/comment", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(con,"Added Comment!",Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(con,"Error adding comment...",Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();

                params.put("shared",String.valueOf(id));
                params.put("user",String.valueOf(commentor));
                params.put("content",message);

                return params;
            }
        };

        req.add(AddComment);
    }
}
