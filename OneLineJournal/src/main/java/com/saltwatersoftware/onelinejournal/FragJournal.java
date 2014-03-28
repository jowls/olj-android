package com.saltwatersoftware.onelinejournal;

//import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.widget.SearchViewCompatIcs;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
public class FragJournal extends Fragment {
    ListView listview;
    ProgressDialog progress;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //new PopulateJournalTask().execute();
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
        progress = new ProgressDialog(getActivity());
        progress.setTitle("Fetching journal");
        progress.setMessage("Please wait...");
        progress.setCancelable(false);
        progress.show();
        View view = inflater.inflate(R.layout.journal_fragment, container, false);
        ListView jListView = (ListView) view.findViewById(R.id.jListView);
        jListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                TextView contentTextView = (TextView) v.findViewById(R.id.contentText);
                TextView dateTextView = (TextView) v.findViewById(R.id.dateText);
                String contentText = contentTextView.getText().toString().trim();
                String dateText = dateTextView.getText().toString().trim();
                FragDayEdit fragDayEdit = new FragDayEdit();
                Bundle bundle = new Bundle();
                bundle.putString("date", dateText);
                bundle.putString("content", contentText);
                fragDayEdit.setArguments(bundle);
                MainActivity activity = (MainActivity) getActivity();
                activity.editDay(fragDayEdit);
                //activity.onNavigationDrawerItemSelected(3);
                //MainActivity.fragmentManager.beginTransaction().replace(R.id.container, fragDayEdit).commit();
            }
        });
        new PopulateJournalTask().execute();
        //return inflater.inflate(R.layout.journal_fragment, container, false);
        return view;
    }

    public void PopulateJournal()
    {
        new PopulateJournalTask().execute();
    }

    class yourAdapter extends BaseAdapter {

        Context context;
        JSONArray data;
        Cursor listCursor;
        private LayoutInflater inflater = null;

        public yourAdapter(Context context, JSONArray data) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.data = data;
            if (data == null)
            {
                this.listCursor = MainActivity.database.query("days", new String[]{"date", "content"}, null, null, null, null, String.format("%s DESC", "date"));
            }
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (data != null)
            {
                return data.length();
            }
            else
            {
                return listCursor.getCount();
            }
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
//            try {
//
//                JSONObject tempObj = data.getJSONObject(position);
//                JSONObject tempDay = tempObj.getJSONObject("day");
//                content.setText(tempDay.optString("content", "NULL"));
//                date.setText(tempDay.optString("date", "NULL"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            //ArrayList<Food> foods = new ArrayList(this);
            listCursor = MainActivity.database.query("days", new String[]{"date", "content"}, null, null, null, null, String.format("%s DESC", "date"));
            //listCursor.moveToFirst();
            listCursor.moveToPosition(position);
            //Food t;
            if(! listCursor.isAfterLast()) {
                    date.setText(listCursor.getString(0));
                    content.setText(listCursor.getString(1));
                    //String name= listCursor.getString(1);
            }
            listCursor.close();
            return vi;
        }
    }
    public class PopulateJournalTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected String doInBackground(Void... unused) {

            HttpClient httpclient = new DefaultHttpClient();

            String token  = MainActivity.sharedPreferences.getString("token", "None");
            Long dbUpdatedMillis = MainActivity.sharedPreferences.getLong("db_updated", -1);
            Time now = new Time();
            now.setToNow();
            Long nowMillis = now.toMillis(true);
            String responseAsText = "Exception";

            if ((dbUpdatedMillis < 0 || (nowMillis-dbUpdatedMillis > 7200000))&& isAdded()) //7200000
            {
                try {
                    JSONObject jsonPost = new JSONObject();
                    jsonPost.put("at", token);

                    String urlPost = getString(R.string.alldays);

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
            }
            return responseAsText;
        }

        protected void onPostExecute(String responseAsText) {
            Log.w("salt", responseAsText);
            try {
                JSONArray jsonJournal;
                if (responseAsText != "Exception" && responseAsText != "Client protocol exception" && responseAsText != "IO Exception")
                {
                    jsonJournal = new JSONArray(responseAsText);

                    for (int i = 0; i < jsonJournal.length(); i++)
                    {
                        JSONObject tempObj = jsonJournal.getJSONObject(i);
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
//                    todo: handle android.database.sqlite.SQLiteConstraintException: column date is not unique (code 19)
                        Long id = MainActivity.database.insert("days", null, values);
                        Log.w("Successfully created row in days table with id: ", id.toString());
                        Time now = new Time();
                        now.setToNow();
                        MainActivity.editor.putLong("db_updated", now.toMillis(true));
                        MainActivity.editor.apply();
                        progress.dismiss();
                    }
                }else
                {
                    jsonJournal = null;
                    progress.dismiss();
                }
                View vw = getView();
                if (vw != null)
                {
                    int id = R.id.jListView;
                    ListView lv = (ListView)vw.findViewById(id);
                    //listview = (ListView) getView().findViewById(R.id.jListView);
                    listview = lv;
                    listview.setAdapter(new yourAdapter(getView().getContext(), jsonJournal));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progress.dismiss();
            }
        }
    }
}

