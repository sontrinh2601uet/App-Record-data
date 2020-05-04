package com.example.sensorrecord.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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

import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;

import static android.content.Context.MODE_PRIVATE;

public class NewFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    MainActivity mainActivity;
    CoordinatorLayout coordinatorLayout;

    TextInputLayout nameWrapper;
    TextInputLayout ageWrapper;
    TextInputLayout jobWrapper;
    TextView versionTextView;

    RadioGroup genderGroup;
    Button createButton;

    public NewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);

        mainActivity = (MainActivity) getActivity();
        checkFirstLogin();
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        mainActivity.navigationView.setCheckedItem(R.id.nav_new);
        mainActivity.setTitle("New User");

        String[] values = {"Android 5: Lollipop", "Android 6: Marshmallow", "Android 7: Nougat", "Android 8: Oreo", "Android 9: Pie", "Android 10"};
        Spinner spinner = (Spinner) view.findViewById(R.id.input_version_spinner);
        ArrayAdapter<String> LTRadapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(LTRadapter);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainActivity.androidVersionSpinner = true;
                return false;
            }
        });

        spinner.setOnItemSelectedListener(this);

        nameWrapper = (TextInputLayout) view.findViewById(R.id.input_name_wrapper);
        ageWrapper = (TextInputLayout) view.findViewById(R.id.input_age_wrapper);
        jobWrapper = (TextInputLayout) view.findViewById(R.id.input_job_wrapper);
        versionTextView = (TextView) view.findViewById(R.id.input_name_device_version);

        createButton = (Button) view.findViewById(R.id.input_submit);
        createButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    private void checkFirstLogin() {
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        if(pref.getBoolean("isLogin", false)) {

            mainActivity.navigationView.getMenu().findItem(R.id.nav_start).setEnabled(true);
            mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setTitle("User Info");

            mainActivity.addFragment(new UserInfoFragment(), false);
        }
    }

    private void saveLogin(String name, String age, String job, String gender, String device) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("pref", MODE_PRIVATE).edit();

        editor.putBoolean("isLogin", true);
        editor.putString("name", name);
        editor.putString("age", age);
        editor.putString("job", job);
        editor.putString("gender", gender);
        editor.putString("device", device);

        editor.commit();
    }

    //Height unit spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                versionTextView.setText(R.string.version_device_5);
                break;
            case 1:
                versionTextView.setText(R.string.version_device_6);
                break;
            case 2:
                versionTextView.setText(R.string.version_device_7);
                break;
            case 3:
                versionTextView.setText(R.string.version_device_8);
                break;
            case 4:
                versionTextView.setText(R.string.version_device_9);
                break;
            case 5:
                versionTextView.setText(R.string.version_device_10);
                break;
        }
    }

    //Height unit spinner item selection
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
    }

    //Create/submit button click
    @Override
    public void onClick(View v) {
        //Get input values
        String name = nameWrapper.getEditText().getText().toString();
        String age = ageWrapper.getEditText().getText().toString();
        String job = jobWrapper.getEditText().getText().toString();
        String gender = "";
        String version = (String) versionTextView.getText();

        genderGroup = (RadioGroup) mainActivity.findViewById(R.id.input_gender);
        int genderID = genderGroup.getCheckedRadioButtonId();

        if (genderID != -1) {
            View radioButton = genderGroup.findViewById(genderID);
            int radioId = genderGroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) genderGroup.getChildAt(radioId);
            gender = (String) btn.getText();
        }

        //If all the validation passes, submit the form. Else, show errors
        if (!isEmpty(name) & !isEmpty(age) & !isEmpty(job) & genderID != -1) {
            //Turn all errors off
            nameWrapper.setError(null);
            ageWrapper.setError(null);
            jobWrapper.setError(null);
            versionTextView.setError(null);

            MainActivity.subCreated = true;

            //Hide the keyboard on click
            showKeyboard(false, mainActivity);

            //Enable additional menu items/fragments for recording and saving sensor data
            mainActivity.navigationView.getMenu().findItem(R.id.nav_start).setEnabled(true);
            mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setTitle("User Info");

            Snackbar.make(coordinatorLayout, "Subject created", Snackbar.LENGTH_SHORT).show();

            saveLogin(name, age, job, gender, version);
            //Change fragment to subject info screen. Do not add this fragment to the backStack
            mainActivity.addFragment(new UserInfoFragment(), false);

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
