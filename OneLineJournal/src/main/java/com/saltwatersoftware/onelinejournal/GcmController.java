package com.saltwatersoftware.onelinejournal;

import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class GcmController extends Application{

    public static final String PROPERTY_REG_ID = "regid";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "~~~REMOVED~~~";
    GoogleCloudMessaging gcm;
    String regid;
    private  final int MAX_ATTEMPTS = 10;
    private  final int BACKOFF_MILLI_SECONDS = 2000;
    private  final Random random = new Random();
    private String TAG  = "GcmController";
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(Activity inActivity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, inActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.w(TAG, "This device is not supported.");
                inActivity.finish();
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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(getApplicationContext());
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
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
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
                    storeRegistrationId(getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

                    for (int i = 1; i <= MAX_ATTEMPTS; i++) {

                        Log.d(TAG, "Attempt #" + i + " to register");

                        try {
                            if (gcm == null) {
                                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Your implementation here.
        Log.i(TAG, "The regid is: " + regid);
        HttpClient httpclient = new DefaultHttpClient();
        String token  = prefs.getString("token", "None");
        String tz = prefs.getString("timezone", "None");
        Log.i(TAG, "The time zone is: " + tz);
        boolean success = false;
        try
        {
            JSONObject jsonPost = new JSONObject();
            jsonPost.put("at", token);
            jsonPost.put("regid", regid);
            jsonPost.put("tz", tz);

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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    public void onCreateRegIdTasks(Activity inActivity)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int av = getAppVersion(getApplicationContext());
        //if ( av != prefs.getInt(PROPERTY_APP_VERSION, 0) ) {
        //todo: switch back to other line before production build
        if ( true ) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regid", "");
            editor.commit();
        }
        if (checkPlayServices(inActivity)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    public void onResumeRegIdTasks(Activity inActivity){
        checkPlayServices(inActivity);
    }

    public void onBootRegIdTasks() {
        regid = getRegistrationId(getApplicationContext());
        //If the regid is not empty then we can assume the checkplayservices has been done in the past
        if (!regid.isEmpty()) {
            registerInBackground();
        }
    }
}