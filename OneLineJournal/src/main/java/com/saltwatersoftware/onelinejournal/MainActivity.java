package com.saltwatersoftware.onelinejournal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, DatePickerFragment.OnDateSelectedListener {
    EditText editText2;
    public static SharedPreferences sharedPreferences;
    public static SqlOpenHelper helper;
    public static SQLiteDatabase database;
    public static SharedPreferences.Editor editor;
    public static String date = "(Choose date) -->";
    public static String content = "";
    public static ProgressDialog jprogress;
    public static FragmentManager fragmentManager;

    //GCM Stuff
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "regid";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "~~~REMOVED~~~"; //Dev SENDER_ID
    GoogleCloudMessaging gcm;
    Context context;
    String regid;
    private  final int MAX_ATTEMPTS = 10;
    private  final int BACKOFF_MILLI_SECONDS = 2000;
    private  final Random random = new Random();
    private String TAG  = "MainActivity";

    View global_view;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getTitle();
        mTitle = "Add Day";
//        mFragmentManager = getSupportFragmentManager();
//        FragmentAddDay fragment = (FragmentAddDay)mFragmentManager.findFragmentById(R.id.container);
//        // This may give ClassCastExceptions in case there are fragments of
//        // different types added to the container, so in that case use the
//        // findFragmentByTag method
//
//        if (fragment == null) {
//            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
//            fragmentTransaction.add(R.id.container, new FragmentAddDay());
//            fragmentTransaction.commit();
//        }
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //db
        helper = new SqlOpenHelper(this);
        database = helper.getWritableDatabase();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        final SharedPreferences prefs = sharedPreferences;
        SharedPreferences.Editor editor = prefs.edit();
        //Remove the next two lines before deployment
        editor.putString("regid", "");
        editor.commit();
        context = getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("mTitle", mTitle.toString());
        super.onSaveInstanceState(savedInstanceState);

        // etc.

    }
    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        mTitle = savedInstanceState.getString("mTitle");
    }

    public void onDateChanged(int year, int month, int day) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
        FragmentAddDay dpFrag = (FragmentAddDay)
                getSupportFragmentManager().findFragmentById(R.id.container);

        if (dpFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            dpFrag.updateDate(year, month, day);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position == 0)
        {
            mTitle = "Add Day";
            fragmentManager.beginTransaction().replace(R.id.container, new FragmentAddDay()).commit();
        }
        else if (position == 1)
        {
            mTitle = "Journal";
            JournalFragment jf = new JournalFragment();
            fragmentManager.beginTransaction().replace(R.id.container, jf).commit();
            //jf.PopulateJournal();
        }
        else if (position == 2)
        {
            mTitle = "About";
            AboutFragment af = new AboutFragment();
            fragmentManager.beginTransaction().replace(R.id.container, af).commit();
            //jf.PopulateJournal();
        }
        else if (position == 3)
        {
            mTitle = "Edit Day";
            DayEditFragment def = new DayEditFragment();
            fragmentManager.beginTransaction().replace(R.id.container, def).commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_example:
                LogOut();
                return true;

            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void LogOut(){
        //editor.putString("token", token);
        DestroyToken();
        database.delete("days", null, null);
        content = "";
        date = "(Choose date) -->";
        editor.putLong("db_updated", -1);
        editor.apply();
        Intent myIntent=new Intent(MainActivity.this,Login.class);
        startActivity(myIntent);
        finish();
    }
    public void DestroyToken()
    {
        editor.remove("token");
        editor.apply();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
        File dbFile=context.getDatabasePath(dbName);
        return dbFile.exists();
    }
    public void showJProgress()
    {
        jprogress = new ProgressDialog(this);
        jprogress.setTitle("Fetching journal");
        jprogress.setMessage("Please wait...");
        jprogress.show();
    }
    public void editDay(DayEditFragment day)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        day.setRetainInstance(true);
        fragmentManager.beginTransaction().replace(R.id.container, day).addToBackStack(null).commit();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.w("salt", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = sharedPreferences;
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    boolean didItSend = sendRegistrationIdToBackend();
                    if (didItSend == false) {
                        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
                        for (int i = 1; i <= MAX_ATTEMPTS; i++) {

                            Log.d(TAG, "Attempt #" + i + " to register");
                            didItSend = sendRegistrationIdToBackend();
                            if (didItSend == true) {
                                break;
                            }
                            if (i == MAX_ATTEMPTS) {
                                break;
                            }
                            try {
                                Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                                Thread.sleep(backoff);
                            } catch (InterruptedException e1) {
                                // Activity finished before we complete - exit.
                                Log.d(TAG, "Thread interrupted: abort remaining retries!");
                                Thread.currentThread().interrupt();
                            }
                            backoff *= 2;
                        }
                    }
                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

                    for (int i = 1; i <= MAX_ATTEMPTS; i++) {

                        Log.d(TAG, "Attempt #" + i + " to register");

                        try {
                            if (gcm == null) {
                                gcm = GoogleCloudMessaging.getInstance(context);
                            }
                            regid = gcm.register(SENDER_ID);
                            msg = "Device registered, registration ID=" + regid;
                        } catch (IOException e) {
                            // Here we are simplifying and retrying on any error; in a real
                            // application, it should retry only on unrecoverable errors
                            // (like HTTP error code 503).
                            Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                            if (i == MAX_ATTEMPTS) {
                                break;
                            }
                            try {
                                Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                                Thread.sleep(backoff);
                            } catch (InterruptedException e1) {
                                // Activity finished before we complete - exit.
                                Log.d(TAG, "Thread interrupted: abort remaining retries!");
                                Thread.currentThread().interrupt();
                            }

                            // increase backoff exponentially
                            backoff *= 2;
                        }
                    }
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private boolean sendRegistrationIdToBackend() {
        // Your implementation here.
        Log.i(TAG, "The regid is: " + regid);
        HttpClient httpclient = new DefaultHttpClient();
        String token  = sharedPreferences.getString("token", "None");
        boolean success = false;
        try
        {
            JSONObject jsonPost = new JSONObject();
            jsonPost.put("at", token);
            jsonPost.put("regid", regid);

            String urlPost = getString(R.string.gcmregid);

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
                success = true;
            }

        } catch (ClientProtocolException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
        return success;
    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = sharedPreferences;
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}