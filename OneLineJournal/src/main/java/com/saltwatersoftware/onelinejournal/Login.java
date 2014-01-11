package com.saltwatersoftware.onelinejournal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by j on 01/11/13.
 */
public class Login extends Activity {
    
    Button mButton;
    EditText mEditEmail;
    EditText mEditPassword;
    View global_view;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ProgressDialog progress;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); //getPreferences(0);
        editor = sharedPreferences.edit();
        String tokenCheck = sharedPreferences.getString("token", "None");
        if (tokenCheck == "None")
        {
            setContentView(R.layout.login);
        }else{
            Intent myIntent=new Intent(Login.this,MainActivity.class);
            startActivity(myIntent);
            finish();
        }
    }
    public void signIn(View view)
    {
        global_view = view;
        progress = new ProgressDialog(this);
        progress.setTitle("Signing in");
        progress.setMessage("Please wait...");
        progress.setCancelable(false);
        progress.show();
// To dismiss the dialog
        new LoginTask().execute();

//        httpget.setHeader("Accept", "application/json");
//        httpget.setHeader("Content-type", "application/json");
//        HttpResponse response = httpclient.execute(httpget);
    }
    public class LoginTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected String doInBackground(Void... unused) {

            HttpClient httpclient = new DefaultHttpClient();
            //get the parameters
            mEditEmail   = (EditText)findViewById(R.id.editText);
            mEditPassword = (EditText)findViewById(R.id.editText4);
            String email = mEditEmail.getText().toString();
            String password = mEditPassword.getText().toString();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            String responseAsText;


            //        String url = "http://www.site.com/login.json?session[email]=" + email + "?session[password]=" + password;
            String urlPost = getString(R.string.tokens_login);

    //        HttpGet httpget = new HttpGet(url);
            try
            {
                HttpPost httppost = new HttpPost(urlPost);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);

                responseAsText = EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                responseAsText = "Exception";

            } catch (IOException e) {
                responseAsText = "Exception";
            }
            return responseAsText;
        }
        protected void onPostExecute(String responseAsText) {
            //mEditEmail.setText(responseAsText); works ok but was too large.
            Log.w("salt", responseAsText);
            if (responseAsText == "Exception")
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setMessage("Sorry an error occurred. Please check login info and network connection.").setTitle(getString(R.string.addday_error_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();
            }else
            {
                try {
                    JSONObject jObject = new JSONObject(responseAsText);
                    String token = jObject.optString("token", null);
                    if (token != null)
                    {
                        editor.putString("token", token);
                        editor.commit();
                        Intent myIntent=new Intent(global_view.getContext(),MainActivity.class);
                        
                        try {
                            progress.dismiss();
                            progress = null;
                        } catch (Exception e) {
                            // nothing
                        }
                        startActivity(myIntent);
                        finish();
                    }else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                        builder.setMessage(jObject.optString("message", null)).setTitle(getString(R.string.addday_error_title));
                        AlertDialog dialog = builder.create();
                        progress.dismiss();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setMessage(e.getMessage()).setTitle(getString(R.string.addday_error_title));
                    AlertDialog dialog = builder.create();
                    progress.dismiss();
                    dialog.show();
                }
            }
        }
    }
}