package com.saltwatersoftware.onelinejournal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;
import java.util.TimeZone;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, FragDatePicker.OnDateSelectedListener {
    EditText editText2;
    public static SharedPreferences sharedPreferences;
    public static SqlOpenHelper helper;
    public static SQLiteDatabase database;
    public static SharedPreferences.Editor editor;
    public static String date = "(Choose date) -->";
    public static String content = "";
    public static ProgressDialog jprogress;
    public static FragmentManager fragmentManager;
    public static ArrayList<String> TZ1;
    public static ArrayAdapter<CharSequence> adapter;

    Context context;
    private GcmController gcmc;

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
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //DB
        helper = new SqlOpenHelper(this);
        database = helper.getWritableDatabase();
        //GCM
        String[]TZ = TimeZone.getAvailableIDs();
        TZ1 = new ArrayList<String>();
        for(int i = 0; i < TZ.length; i++) {
            if(!(TZ1.contains(TimeZone.getTimeZone(TZ[i]).getID()))) {
                TZ1.add(TimeZone.getTimeZone(TZ[i]).getID());
            }
        }
        if (sharedPreferences.getString("timezone", "None") == "None") {
            TimeZone tz = TimeZone.getDefault();
            Log.i("MAIN", "Time zone is: " + tz.getID());
            editor.putString("timezone", tz.getID());
            editor.commit();
        }
        adapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 0; i < MainActivity.TZ1.size(); i++) {
            adapter.add(MainActivity.TZ1.get(i));
        }
        if (!sharedPreferences.contains("push"))
        {
            editor.putBoolean("push", true);
        }
        gcmc = (GcmController) getApplicationContext();
        gcmc.onCreateRegIdTasks(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gcmc.onResumeRegIdTasks(this);
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
        FragAddDay dpFrag = (FragAddDay)
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
            fragmentManager.beginTransaction().replace(R.id.container, new FragAddDay()).commit();
        }
        else if (position == 1)
        {
            mTitle = "Journal";
            FragJournal jf = new FragJournal();
            fragmentManager.beginTransaction().replace(R.id.container, jf).commit();
            //jf.PopulateJournal();
        }
        else if (position == 2)
        {
            mTitle = "About";
            FragAbout af = new FragAbout();
            fragmentManager.beginTransaction().replace(R.id.container, af).commit();
            //jf.PopulateJournal();
        }
        else if (position == 3)
        {
            mTitle = "Settings";
            FragSettings set = new FragSettings();
            fragmentManager.beginTransaction().replace(R.id.container, set).commit();
        }
        else if (position == 4)
        {
            mTitle = "Edit Day";
            FragDayEdit def = new FragDayEdit();
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

            //case R.id.action_settings:
                //return true;
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
        //editor.remove("timezone");
        editor.apply();
        Intent myIntent=new Intent(MainActivity.this,LoginActivity.class);
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
        DialogFragment newFragment = new FragDatePicker();
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
    public void editDay(FragDayEdit day)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        day.setRetainInstance(true);
        fragmentManager.beginTransaction().replace(R.id.container, day).addToBackStack(null).commit();
    }

}