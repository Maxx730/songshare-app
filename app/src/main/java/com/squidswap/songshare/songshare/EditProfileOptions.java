package com.squidswap.songshare.songshare;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class EditProfileOptions extends AppCompatActivity {

    private RequestQueue req;
    private SharedPreferences prefs;
    private EditText profileField;
    private Button SaveChanges,CancelChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_edit_profile_options);

        req = Volley.newRequestQueue(getApplicationContext());
        prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);

        //Grab UI elements here.
        profileField = findViewById(R.id.ProfileValueField);
        SaveChanges = findViewById(R.id.SaveChangedButton);
        CancelChanges = findViewById(R.id.CancelEditProfile);

        //LOAD THE USER INFO HERE.
        StringRequest userInf = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/"+String.valueOf(prefs.getInt("SongShareId",0)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray responses = new JSONArray(response);
                    JSONObject obj = responses.getJSONObject(0);

                    profileField.setText(obj.getString("profile"));
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Error pull user data from server...",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        req.add(userInf);

        SaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest applyChanges = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/change/profile", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(),"Changes Saved",Toast.LENGTH_SHORT).show();
                        finish();
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
                        params.put("profile",profileField.getText().toString());
                        return params;
                    }
                };
                req.add(applyChanges);
            }
        });

        CancelChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(EditProfileOptions.this).create();
                alertDialog.setTitle("Are you sure you would like to cancel changes without saving?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.hide();
                    }
                });
                alertDialog.show();
            }
        });
    }
}
