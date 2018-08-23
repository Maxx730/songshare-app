package com.squidswap.songshare.songshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameField,passwordField,repeatPassword,emailField;
    private Button signUpBtn;
    private TextView warning;
    private SharedPreferences prefs;
    private RequestQueue req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up);

        usernameField = findViewById(R.id.UsernameField);
        passwordField = findViewById(R.id.PasswordField);
        repeatPassword = findViewById(R.id.RepeatPassword);
        emailField = findViewById(R.id.EmailField);
        signUpBtn = findViewById(R.id.SignUpButton);
        warning = findViewById(R.id.WarningText);
        req = Volley.newRequestQueue(getApplicationContext());

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(usernameField.getText()) && !TextUtils.isEmpty(passwordField.getText()) && !TextUtils.isEmpty(repeatPassword.getText()) && !TextUtils.isEmpty(emailField.getText()) && TextUtils.equals(passwordField.getText(),repeatPassword.getText())){
                    StringRequest createUser = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/user/create", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                            try{
                                JSONObject returnObj = new JSONObject(response);

                                if(returnObj.getString("STATUS").equals("SUCCESS")){
                                    Intent i = new Intent(getApplicationContext(),FindFriends.class);
                                    SharedPreferences prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("SongShareUser",usernameField.getText().toString());
                                    edit.putString("SongSharePassword",passwordField.getText().toString());
                                    edit.putInt("SongShareId",returnObj.getJSONObject("PAYLOAD").getInt("insertId"));
                                    edit.commit();
                                    startActivity(i);
                                }else{
                                    Toast.makeText(getApplicationContext(),"There seems to have been a problem creating an account.",Toast.LENGTH_LONG).show();
                                }
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
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("username",usernameField.getText().toString());
                            params.put("password",passwordField.getText().toString());
                            params.put("email",emailField.getText().toString());
                            return params;
                        }
                    };
                    req.add(createUser);
                }else{
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
