package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextView SignUpLink,errorText;
    private SharedPreferences prefs;
    private Button loginButton;
    private EditText usernameField,passwordField;
    private String username,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignUpLink = findViewById(R.id.SignUpLink);
        loginButton = findViewById(R.id.LoginButton);
        usernameField = findViewById(R.id.UsernameField);
        passwordField = findViewById(R.id.PasswordField);
        errorText = findViewById(R.id.ErrorText);


        SignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signScreen = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(signScreen);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(usernameField.getText()) && !TextUtils.isEmpty(passwordField.getText())){
                    username = usernameField.getText().toString();
                    password = passwordField.getText().toString();

                    CheckLogin();
                }else{
                    Toast.makeText(getApplicationContext(),"Please fill out both username and password fields.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void CheckLogin(){
        RequestQueue quest = Volley.newRequestQueue(getApplicationContext());
        String url = "http://104.236.66.72:5698/user/login";

        StringRequest loginRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(null != response){
                    try{
                        JSONObject rep = new JSONObject(response);

                        if(rep.getString("TYPE").equals("SUCCESS")){
                            prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
                            JSONObject payload = rep.getJSONObject("PAYLOAD");
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("SongShareUser",username);
                            edit.putString("SongSharePassword",password);
                            edit.putInt("SongShareId",payload.getInt("_id"));
                            edit.commit();
                            Intent i = new Intent(getApplicationContext(),StreamView.class);
                            startActivity(i);
                        }else{
                            errorText.setVisibility(View.VISIBLE);
                        }
                    }catch(JSONException e){

                    }

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
                params.put("username",username);
                params.put("password",password);
                return params;
            }
        };
        quest.add(loginRequest);
    }
}
