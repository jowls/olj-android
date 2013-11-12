package com.saltwatersoftware.onelinejournal;

//import android.app.Fragment;
import android.content.ContentValues;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.support.v4.widget.SearchViewCompatIcs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
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
 * Created by j on 11/11/13.
 */
public class JournalFragment extends Fragment {
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //new PopulateJournalTask().execute();
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
        new PopulateJournalTask().execute();
        return inflater.inflate(R.layout.journal_fragment, container, false);

    }

    public void PopulateJournal()
    {
        new PopulateJournalTask().execute();
    }

    class yourAdapter extends BaseAdapter {

        Context context;
        JSONArray data;
        private LayoutInflater inflater = null;

        public yourAdapter(Context context, JSONArray data) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.length();
        }

        @Override
        public JSONObject getItem(int position) {
            // TODO Auto-generated method stub

            try {
                return data.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.row, null);
            TextView date = (TextView) vi.findViewById(R.id.dateText);
            TextView content = (TextView) vi.findViewById(R.id.contentText);
            try {
                JSONObject tempObj = data.getJSONObject(position);
                JSONObject tempDay = tempObj.getJSONObject("day");
                content.setText(tempDay.optString("content", "NULL"));
                date.setText(tempDay.optString("date", "NULL"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return vi;
        }
    }
    public class PopulateJournalTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected String doInBackground(Void... unused) {

            //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
            HttpClient httpclient = new DefaultHttpClient();

            String token  = MainActivity.sharedPreferences.getString("token", "None");
            String responseAsText = "Exception";

            try {
                JSONObject jsonPost = new JSONObject();
                jsonPost.put("at", token);

                String urlPost = "http://oljtrial.cloudapp.net/api/v1/mobiles/alldays";

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
            try {
//                JSONObject jsonJournal = new JSONObject(responseAsText);
                JSONArray jsonJournal = new JSONArray(responseAsText);

                listview = (ListView) getView().findViewById(R.id.jListView);
                listview.setAdapter(new yourAdapter(getView().getContext(), jsonJournal));

                for (int i = 0; i < jsonJournal.length(); i++)
                {
                    JSONObject tempObj = jsonJournal.getJSONObject(i);
                    JSONObject tempDay = tempObj.getJSONObject("day");
                    String content = tempDay.optString("content", "NULL");
                    String date = tempDay.optString("date", "NULL");
                    ContentValues values = new ContentValues();
                    values.put("date", date);
                    values.put("content", content);
                    values.put("updated_at", content);
//                    todo: handle android.database.sqlite.SQLiteConstraintException: column date is not unique (code 19)
                    Long id = MainActivity.database.insert("days", null, values);
                    Log.w("Successfully created row in days table with id: ", id.toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

