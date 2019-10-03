package com.example.sensorrecord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class NewFragment extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    MainActivity mainActivity;
    CoordinatorLayout coordinatorLayout;
    ExportCSV exportCSV;
    SharedPreferences shareStatus;

    TextInputLayout nameWrapper;
    TextInputLayout ageWrapper;
    TextInputLayout jobWrapper;
    TextView versionTextView;

    String sex;
    RadioGroup sexGroup;
    Button createButton;

    public NewFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        shareStatus = getSharedPreferences("shareStatus", 0);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        mainActivity = new MainActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_new);

        mainActivity.setTitle("New Participant");

        String[] values = {"Android 5: Lollipop", "Android 6: Marshmallow", "Android 7: Nougat", "Android 8: Oreo", "Android 9: Pie", "Android 10"};
        Spinner spinner = (Spinner) findViewById(R.id.input_version_spinner);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        listAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(listAdapter);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainActivity.deviceSpinnerTouched = true;
                return false;
            }
        });

        spinner.setOnItemSelectedListener(this);

        nameWrapper = (TextInputLayout) findViewById(R.id.input_name_wrapper);
        ageWrapper = (TextInputLayout) findViewById(R.id.input_age_wrapper);
        jobWrapper = (TextInputLayout) findViewById(R.id.input_job_wrapper);
        versionTextView = (TextView) mainActivity.findViewById(R.id.input_name_device_version);

        createButton = (Button) findViewById(R.id.input_submit);
        createButton.setOnClickListener(this);
    }

    //Device version spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                versionTextView.setText(R.string.version_device_5);
                break;
            case 2:
                versionTextView.setText(R.string.version_device_6);
                break;
            case 3:
                versionTextView.setText(R.string.version_device_7);
                break;
            case 4:
                versionTextView.setText(R.string.version_device_8);
                break;
            case 5:
                versionTextView.setText(R.string.version_device_9);
                break;
            case 6:
                versionTextView.setText(R.string.version_device_10);
                break;
        }
    }

    //device version spinner item selection
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
    }

    // Create/submit button click
    @Override
    public void onClick(View v) {
        //Get input values
        String name = nameWrapper.getEditText().getText().toString();
        String age = ageWrapper.getEditText().getText().toString();
        String job = jobWrapper.getEditText().getText().toString();
        TextView sexLabel = (TextView) mainActivity.findViewById(R.id.input_label_sex);
        String version = (String) versionTextView.getText();

        sexGroup = (RadioGroup) mainActivity.findViewById(R.id.input_sex);
        int sexID = sexGroup.getCheckedRadioButtonId();

        if (sexID != -1) {
            View radioButton = sexGroup.findViewById(sexID);
            int radioId = sexGroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) sexGroup.getChildAt(radioId);
            sex = (String) btn.getText();
            sexLabel.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
        }

        //If all the validation passes, submit the form. Else, show errors
        if (!isEmpty(name) & !isEmpty(age) & !isEmpty(job) & !isEmpty(version) & sexID != -1) {
            //Turn all errors off
            nameWrapper.setError(null);
            ageWrapper.setError(null);
            jobWrapper.setError(null);
            versionTextView.setError(null);

            exportCSV.createInformation(name, job, Short.parseShort(age), sex, version);

            //Hide the keyboard on click
            showKeyboard(false, mainActivity);

            //Enable additional menu items/fragments for recording and saving sensor data
            mainActivity.navigationView.getMenu().findItem(R.id.nav_start).setEnabled(true);
            mainActivity.navigationView.getMenu().findItem(R.id.nav_save).setEnabled(true);
            mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setTitle("User Information");

            Snackbar.make(coordinatorLayout, "User Information created", Snackbar.LENGTH_SHORT).show();

            //Change fragment to subject info screen. Do not add this fragment to the backstack
            mainActivity.addFragment(new SubjectInfoFragment(), false);

        } else {
            if (isEmpty(name)) {
                nameWrapper.setError("Name required");
            } else {
                nameWrapper.setError(null);
            }

            if (isEmpty(age)) {
                ageWrapper.setError("Age required");
            } else {
                ageWrapper.setError(null);
            }

            if (isEmpty(job)) {
                jobWrapper.setError("Job required");
            } else {
                jobWrapper.setError(null);
            }

            if (isEmpty(version)) {
                versionTextView.setError("Device version required");
            }
            //If no radio button has been selected
            if (sexID == -1) {
                sexLabel.setTextColor(Color.RED);
            }
        }
    }

    //Check if a string is empty
    public boolean isEmpty(String string) {
        return string.equals("");
    }

    public void showKeyboard(Boolean show, MainActivity mainActivity) {
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            // check if no view has focus before hiding keyboard
            View v = mainActivity.getCurrentFocus();
            if (v != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

}
