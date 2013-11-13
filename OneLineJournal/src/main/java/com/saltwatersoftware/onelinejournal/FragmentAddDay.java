package com.saltwatersoftware.onelinejournal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by j on 03/11/13.
 */
public class FragmentAddDay extends Fragment implements View.OnClickListener {
    TextView editText2;
    private DatePickerDialog.OnDateSetListener dateListener;
    public TextView mDate;
    public EditText mContent;
    SharedPreferences sharedPreferences;
    String transDateString;
    Button btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_day, container, false);
        btn = (Button) view.findViewById(R.id.button2);
        btn.setOnClickListener(this);
//        if (savedInstanceState.getString("date") != null)
//        {
//            editText2.setText(savedInstanceState.getString("date"));
//        }
        return view;
    }

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//
//        // Save UI state changes to the savedInstanceState.
//        // This bundle will be passed to onCreate if the process is
//        // killed and restarted.
//
//        savedInstanceState.putString("date", editText2.toString());
//        super.onSaveInstanceState(savedInstanceState);
//
//        // etc.
//
//    }
    @Override
    public void onClick(View v) {
        new AddDayTask().execute();
    }
    public void updateDate(int year, int month, int day) {
        editText2 =   (TextView) this.getView().findViewById(R.id.textView);
        String date = year + "-" + month + "-" + day;
        editText2.setText(date);
    }

    public class AddDayTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected String doInBackground(Void... unused) {

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
            HttpClient httpclient = new DefaultHttpClient();
            //get the parameters
            mDate   = (TextView)getView().findViewById(R.id.textView);
            mContent = (EditText)getView().findViewById(R.id.editText);
            String date = mDate.getText().toString();
            String content = mContent.getText().toString();
            String token  = sharedPreferences.getString("token", "None");
            String responseAsText = "Exception";

            try {
                JSONObject jsonDay = new JSONObject();
                jsonDay.put("date", date);
                jsonDay.put("content", content);
                JSONObject jsonPost = new JSONObject();
                jsonPost.put("at", token);
                jsonPost.put("day", jsonDay);

                String urlPost = "http://oljtrial.cloudapp.net/api/v1/mobiles/addday";

                HttpPost httppost = new HttpPost(urlPost);
                StringEntity se = new StringEntity(jsonPost.toString());
                httppost.setEntity(se);
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");
                HttpResponse response = httpclient.execute(httppost);

                responseAsText = EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                responseAsText = "Client protocol exception";

            } catch (IOException e) {
                responseAsText = "IO Exception";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return responseAsText;
        }

        protected void onPostExecute(String responseAsText) {
            //mEditEmail.setText(responseAsText); works ok but was too large.
            Log.w("salt", responseAsText);
//            try {
//                JSONObject jObject = new JSONObject(responseAsText);
//                String token = jObject.getString("token");
//                if (token != null)
//                {
//                    editor.putString("token", token);
//                    editor.commit();
//                    Intent myIntent=new Intent(global_view.getContext(),MainActivity.class);
//                    startActivity(myIntent);
//                    finish();
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
    }
}
