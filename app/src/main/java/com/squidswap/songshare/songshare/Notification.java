package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

//Simple Notification class
public class Notification {
    private String type,name;
    private int id,send_id;
    private RequestQueue req;

    public Notification(String type,String name,int id,int send_id){
        this.type = type;
        this.name = name;
        this.id = id;
        this.send_id = send_id;
    }

    //Getter/Setter Methods here.
    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }

    public void AcceptNotification(Context con,final SongshareNotificationInterface inter){
        final Context text = con;
        req = Volley.newRequestQueue(con);
        final SharedPreferences prefs = con.getSharedPreferences("SongShareLogin",Context.MODE_PRIVATE);

        switch(this.type){
            case "group":
                break;
            case "friend":
                req.add(new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/friend/accept", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(text,response,Toast.LENGTH_LONG).show();
                        inter.NotificationRecieved();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> params = new HashMap<>();

                        params.put("acceptedId",String.valueOf(id));
                        params.put("user_id",String.valueOf(prefs.getInt("SongShareId",0)));
                        params.put("friend_id",String.valueOf(send_id));

                        return params;
                    }
                });
                break;
            default:
                break;
        }
    }
}
