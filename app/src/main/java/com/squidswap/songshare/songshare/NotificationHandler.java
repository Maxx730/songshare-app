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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Class that will handle checking for notifications as well as will pull different types of Notifications for the user.
public class NotificationHandler{

    private RequestQueue req;
    private ArrayList<Notification> notifs;
    private Context con;
    private String USER_ID;
    private SongshareNotificationInterface notif_interface;

    public NotificationHandler(Context con,String user_id,SongshareNotificationInterface notint){
        this.con = con;
        this.req = Volley.newRequestQueue(this.con);
        this.USER_ID = user_id;
        this.notifs = new ArrayList<>();
        this.notif_interface = notint;

        GetNotifications();
    }

    public void GetNotifications(){
        StringRequest NotifString = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/notifications", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject res = new JSONObject(response);

                    JSONArray friends = res.getJSONArray("FRIENDS");
                    JSONArray groups = res.getJSONArray("GROUPS");

                    //Loop through each of these and create notifications for each one of them.
                    for(int i = 0;i < friends.length();i++){
                        try{
                            notifs.add(new Notification("friend",friends.getJSONObject(i).getString("username"),friends.getJSONObject(i).getInt("_id"),friends.getJSONObject(i).getInt("friend_id")));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                    for(int i = 0;i < groups.length();i++){
                        notifs.add(new Notification("group",groups.getJSONObject(i).getString("title"),friends.getJSONObject(i).getInt("_id"),0));
                    }

                    notif_interface.NotificationRecieved();
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
                params.put("user_id",String.valueOf(USER_ID));
                return params;
            }
        };
        req.add(NotifString);
    }

    public ArrayList<Notification> GetNotifs(){
        return this.notifs;
    }

    //Overide method for doing something when notifications have been recieved.
    public void OnNotificationsRecieved(){
        return;
    }

    public int GetNotifCount(){
        return this.notifs.size();
    }
}
