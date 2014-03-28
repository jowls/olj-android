package com.saltwatersoftware.onelinejournal;

//import android.app.Fragment;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by j on 30/11/13.
 */
public class FragDayEdit extends Fragment implements View.OnClickListener {
    ProgressDialog progress;
    SharedPreferences sharedPreferences;
    public EditText mContent;
    TextView mDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_day, container, false);
        Button btn = (Button) view.findViewById(R.id.editSubmit);
        btn.setOnClickListener(this);
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            String date = bundle.getString("date");
            String content = bundle.getString("content");
            mDate = (TextView) view.findViewById(R.id.dayValueLabel);
            mDate.setText(date);
            mContent = (EditText) view.findViewById(R.id.editText);
            mContent.setText(content);
        }
        //setRetainInstance(true);

        if (savedInstanceState != null) {
            String savedText = savedInstanceState.getString("date");
            String savedContent = savedInstanceState.getString("content");

            mDate = (TextView) view.findViewById(R.id.dayValueLabel);
            mDate.setText(savedText);

            mContent = (EditText) view.findViewById(R.id.editText);
            mContent.setText(savedContent);
        }
        //return inflater.inflate(R.layout.edit_day, container, false);
        return view;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("date", mDate.getText().toString());
        outState.putString("content", mContent.getText().toString());
    }

    @Override
    public void onClick(View v) {
        Button button2 =   (Button) this.getView().findViewById(R.id.editSubmit);
        progress = new ProgressDialog(getActivity());
        progress.setTitle("Submitting changes");
        progress.setMessage("Please wait...");
        progress.setCancelable(false);
        progress.show();
        new EditDayTask().execute();
        button2.setEnabled(true);
    }

    public class EditDayTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected String doInBackground(Void... unused) {

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
            HttpClient httpclient = new DefaultHttpClient();
            //get the parameters
            mContent = (EditText)getView().findViewById(R.id.editText);
            mContent = (EditText)getView().findViewById(R.id.editText);
            String content = mContent.getText().toString();
            String date = mDate.getText().toString();
            String token  = sharedPreferences.getString("token", "None");
            Cursor c = MainActivity.database.query("days", null, "date=?", new String[]{date}, null, null, null);
            int i = c.getColumnIndex("rails_id");
            int j = c.getColumnIndex("updated_at");
            c.moveToFirst();
            int railsID = c.getInt(i);
            String updatedAt = c.getString(j);

            String responseAsText = "Exception";

            try {
                JSONObject jsonDay = new JSONObject();
                jsonDay.put("date", date);
                jsonDay.put("content", content);
                jsonDay.put("rails_id", railsID);
                jsonDay.put("updated_at", updatedAt);
                JSONObject jsonPost = new JSONObject();
                jsonPost.put("at", token);
                jsonPost.put("day", jsonDay);

                String urlPost = getString(R.string.editday);

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
                //todo: finish this up
                JSONArray jsonJournal;
                try {
                    JSONObject tempObj = new JSONObject(responseAsText);
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
                    Integer num = MainActivity.database.update("days", values, "rails_id="+railsID.toString(), null);
                    Log.w("Successfully updated row. # row(s) affected is : ", num.toString());
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
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }
}