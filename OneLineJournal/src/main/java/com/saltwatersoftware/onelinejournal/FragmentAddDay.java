package com.saltwatersoftware.onelinejournal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by j on 03/11/13.
 */
public class FragmentAddDay extends Fragment {
    TextView editText2;
    private DatePickerDialog.OnDateSetListener dateListener;
    public int mYear;
    public int mMonth;
    public int mDay;
    String transDateString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //editText2 = (EditText) getView()
        //Fragment general = getSupportFragmentManager().findFragmentById(R.id.);

        return inflater.inflate(R.layout.fragment_add_day, container, false);
    }
    public void updateDate(int year, int month, int day) {
        editText2 =   (TextView) this.getView().findViewById(R.id.textView);
        String date = ""+ year + month + day;
        editText2.setText(date);
    }
}
