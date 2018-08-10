package com.squidswap.songshare.songshare;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameField,passwordField,repeatPassword,emailField;
    private Button signUpBtn;
    private TextView warning;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameField = findViewById(R.id.UsernameField);
        passwordField = findViewById(R.id.PasswordField);
        repeatPassword = findViewById(R.id.RepeatPassword);
        emailField = findViewById(R.id.EmailField);
        signUpBtn = findViewById(R.id.SignUpButton);
        warning = findViewById(R.id.WarningText);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(usernameField.getText()) && !TextUtils.isEmpty(passwordField.getText()) && !TextUtils.isEmpty(repeatPassword.getText()) && !TextUtils.isEmpty(emailField.getText()) && TextUtils.equals(passwordField.getText(),repeatPassword.getText())){
                    SignUpConnect con = new SignUpConnect();
                    con.execute(usernameField.getText().toString(),passwordField.getText().toString(),emailField.getText().toString());
                }else{
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    class SignUpConnect extends AsyncTask<String,Void,JSONObject>{

        private HttpURLConnection con;
        private URL url;
        private OutputStream out;
        private JSONObject params;
        private DataOutputStream dos;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                url = new URL("http://104.236.66.72:5698/user/create");
                params = new JSONObject();

                params.put("username",strings[0]);
                params.put("password",strings[1]);
                params.put("email",strings[2]);

                con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(15000);
                con.setConnectTimeout(15000);
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type","application/json");
                con.setRequestMethod("POST");

                dos = new DataOutputStream(con.getOutputStream());
                dos.writeBytes(params.toString());
                dos.flush();
                dos.close();

                System.out.println(con.getResponseCode());
                if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
                    prefs = getSharedPreferences("SongShareLogin",MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("SongShareUser",params.getString("username"));
                    edit.putString("SongSharePassword",params.getString("password"));
                    edit.commit();
                }else{
                    System.out.println("we are not working");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
