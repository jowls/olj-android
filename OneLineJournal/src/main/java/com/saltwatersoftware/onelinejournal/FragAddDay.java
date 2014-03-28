package com.saltwatersoftware.onelinejournal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by j on 03/11/13.
 */
public class FragAddDay extends Fragment implements View.OnClickListener {
    private DatePickerDialog.OnDateSetListener dateListener;
    public TextView mDate;
    public EditText mContent;
    SharedPreferences sharedPreferences;
    String transDateString;
    Button btn;
    ProgressDialog progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_day, container, false);
        btn = (Button) view.findViewById(R.id.button2);
        btn.setOnClickListener(this);
        if (savedInstanceState != null) {
            String savedText = savedInstanceState.getString("date");
            String savedContent = savedInstanceState.getString("content");
            //mDate =   (TextView) this.getView().findViewById(R.id.textView);
            //View vw = getView();
            mDate = (TextView) view.findViewById(R.id.textView);
            mDate.setText(savedText);
            MainActivity.date = savedText;

            mContent = (EditText) view.findViewById(R.id.editText);
            mContent.setText(savedContent);
            MainActivity.content = savedContent;
        }else
        {
            if (mDate == null || mContent == null)
            {
                mDate = (TextView) view.findViewById(R.id.textView);
                mDate.setText(MainActivity.date);
                mContent = (EditText) view.findViewById(R.id.editText);
                mContent.setText(MainActivity.content);
            }
        }
        return view;
    }
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//
//        // Save UI state changes to the savedInstanceState.
//        // This bundle will be passed to onCreate if the process is
//        // killed and restarted.
//
//        savedInstanceState.putString("date", textView.toString());
//        super.onSaveInstanceState(savedInstanceState);
//
//        // etc.
//
//    }
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    // Read values from the "savedInstanceState"-object and put them in your textview
//    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        mDate =   (TextView) this.getView().findViewById(R.id.textView);
        outState.putString("date", mDate.getText().toString());
        outState.putString("content", mContent.getText().toString());
        //super.onSaveInstanceState(outState);
    }
    @Override
    public void onClick(View v) {
        Button button2 =   (Button) this.getView().findViewById(R.id.button2);
        progress = new ProgressDialog(getActivity());
        progress.setTitle("Adding Day");
        progress.setMessage("Please wait...");
        progress.setCancelable(false);
        progress.show();
        new AddDayTask().execute();
        button2.setEnabled(true);
    }
    public void updateDate(int year, int month, int day) {
        mDate =   (TextView) this.getView().findViewById(R.id.textView);
        month = month + 1;
        String date = year + "-" + month + "-" + day;
        mDate.setText(date);
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

                String urlPost = getString(R.string.addday);

                HttpPost httppost = new HttpPost(urlPost);
                StringEntity se = new StringEntity(jsonPost.toString());
                httppost.setEntity(se);
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                int code = statusLine.getStatusCode();
                if (code == 200)
                {
                    responseAsText = EntityUtils.toString(response.getEntity());
                }
                else if (code == 401)
                {
                    responseAsText = "Unauth";
                }
                else if (code == 422)
                {
                    responseAsText = "Unprocess";
                }
                else                {
                    responseAsText = "Error";
                }

            } catch (ClientProtocolException e) {
                responseAsText = "Client";

            } catch (IOException e) {
                responseAsText = "IO";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return responseAsText;
        }

        protected void onPostExecute(String responseAsText) {
            //mEditEmail.setText(responseAsText); works ok but was too large.
            Log.w("salt", responseAsText);
            if (responseAsText == "Error" || responseAsText == "Exception")
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.addday_error)).setTitle(getString(R.string.addday_error_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();

            }
            else if (responseAsText == "Unauth")
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.addday_unauth)).setTitle(getString(R.string.addday_error_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();
            }
            else if (responseAsText == "IO")
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Please check network connection. Offline support is planned but not yet available.").setTitle(getString(R.string.addday_error_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();
            }
            else if (responseAsText == "Unprocess")
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.addday_unprocess)).setTitle(getString(R.string.addday_error_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();
            }
            else if (responseAsText == "Client")
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.addday_client)).setTitle(getString(R.string.addday_error_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();
            }
            else
            {
                JSONObject tempObj = null;
                try {
                    tempObj = new JSONObject(responseAsText);
                    JSONObject tempDay = tempObj.getJSONObject("day");
                    String content = tempDay.optString("content", "NULL");
                    String date = tempDay.optString("date", "NULL");
                    String updatedAt = tempDay.optString("updated_at", "NULL");
                    Integer railsID = tempDay.optInt("id", -1);
                    ContentValues values = new ContentValues();
                    values.put("date", date);
                    values.put("content", content);
                    values.put("updated_at", updatedAt);
                    values.put("rails_id", railsID);
                    Long id = MainActivity.database.insert("days", null, values);
                    Log.w("Successfully created row in days table with id: ", id.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mDate.setText("(Choose date) -->");
                mContent.setText("");
                MainActivity.date = "(Choose date) -->";
                MainActivity.content = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.addday_success)).setTitle(getString(R.string.addday_success_title));
                AlertDialog dialog = builder.create();
                progress.dismiss();
                dialog.show();
            }
        }
    }
}