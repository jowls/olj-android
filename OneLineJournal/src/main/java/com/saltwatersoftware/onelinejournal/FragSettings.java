package com.saltwatersoftware.onelinejournal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import java.util.TimeZone;

public class FragSettings extends Fragment implements View.OnClickListener {

    SharedPreferences sharedPreferences;

    public FragSettings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final Spinner TZone = (Spinner) view.findViewById(R.id.spinner);
        TZone.setAdapter(MainActivity.adapter);
        String storedTz = MainActivity.sharedPreferences.getString("timezone", null);
        if ( storedTz != null)
        {
            for(int i = 0; i < MainActivity.TZ1.size(); i++) {
                if(MainActivity.TZ1.get(i).equals(storedTz)) {
                    TZone.setSelection(i);
                }
            }
        }else{
            for(int i = 0; i < MainActivity.TZ1.size(); i++) {
                if(MainActivity.TZ1.get(i).equals(TimeZone.getDefault().getID())) {
                    TZone.setSelection(i);
                }
            }
        }

        TZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                String tz = TZone.getSelectedItem().toString();
                MainActivity.editor.putString("timezone", tz);
                MainActivity.editor.commit();
            }
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
        Button button = (Button) view.findViewById(R.id.button);
        Boolean isOn = MainActivity.sharedPreferences.getBoolean("push", true);
        if (isOn == false) {
            button.setText("OFF");
        } else {
            button.setText("ON");
        }
        button.setOnClickListener(this);
        return view;
    }
    /*public View CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isChecked) {
                editor.putBoolean("push", true);
                editor.apply();
            } else {
                editor.putBoolean("push", false);
                editor.apply();
            }
        }
    }*/
    public void onClick(View v) {
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        Button butt = (Button) v.findViewById(R.id.button);
        if (butt.getText() == "OFF") {
            butt.setText("ON");
            editor.putBoolean("push", true);
            editor.apply();
        } else {
            butt.setText("OFF");
            editor.putBoolean("push", false);
            editor.apply();
        }
    }
}