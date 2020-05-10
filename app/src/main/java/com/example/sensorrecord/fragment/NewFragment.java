package com.example.sensorrecord.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;

import static android.content.Context.MODE_PRIVATE;

public class NewFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    MainActivity mainActivity;
    CoordinatorLayout coordinatorLayout;

    TextInputLayout nameWrapper;
    TextInputLayout ageWrapper;
    TextInputLayout jobWrapper;
    EditText heightCM;
    EditText heightFT;
    EditText heightIN;
    TextView heightFTSym;
    TextView heightINSym;

    RadioGroup genderGroup;
    Button createButton;

    Integer height;
    String heightUnit;
    Boolean heightEntered;

    public NewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);

        mainActivity = (MainActivity) getActivity();
        checkFirstLogin();
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        mainActivity.navigationView.setCheckedItem(R.id.nav_new);
        mainActivity.setTitle("New User");

        String[] values = {"cm", "ft/in"};
        Spinner spinner = (Spinner) view.findViewById(R.id.input_height_spinner);
        ArrayAdapter<String> LTRadapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(LTRadapter);

        //Set a flag for when the user manually chooses the spinner (for focus setting on height EditText)
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainActivity.heightUnitSpinnerTouched = true;
                return false;
            }
        });

        spinner.setOnItemSelectedListener(this);

        nameWrapper = (TextInputLayout) view.findViewById(R.id.input_name_wrapper);
        ageWrapper = (TextInputLayout) view.findViewById(R.id.input_age_wrapper);
        jobWrapper = (TextInputLayout) view.findViewById(R.id.input_job_wrapper);

        heightCM = (EditText) view.findViewById(R.id.input_height_cm);
        heightFT = (EditText) view.findViewById(R.id.input_height_ft);
        heightIN = (EditText) view.findViewById(R.id.input_height_in);
        heightFTSym = (TextView) view.findViewById(R.id.input_label_height_ft_symbol);
        heightINSym = (TextView) view.findViewById(R.id.input_label_height_in_symbol);

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

    private void saveLogin(String name, String age, String job, String gender) {
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("pref", MODE_PRIVATE).edit();

        int previousIdUsed = pref.getInt("id", 0);

        editor.putInt("id", previousIdUsed + 1);
        editor.putBoolean("isLogin", true);
        editor.putString("name", name);
        editor.putString("age", age);
        editor.putString("job", job);
        editor.putString("gender", gender);
        editor.putInt("height", height);
        editor.putString("heightUnit", heightUnit);

        editor.commit();
    }

    //Create/submit button click
    @Override
    public void onClick(View v) {
        //Get input values
        String name = nameWrapper.getEditText().getText().toString();
        String age = ageWrapper.getEditText().getText().toString();
        String job = jobWrapper.getEditText().getText().toString();
        String heightCMValue = heightCM.getText().toString();
        String heightFTValue = heightFT.getText().toString();
        String heightINValue = heightIN.getText().toString();
        String gender = "";
        TextView heightLabel = (TextView) mainActivity.findViewById(R.id.input_label_height);

        genderGroup = (RadioGroup) mainActivity.findViewById(R.id.input_gender);
        int genderID = genderGroup.getCheckedRadioButtonId();

        if (genderID != -1) {
            View radioButton = genderGroup.findViewById(genderID);
            int radioId = genderGroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) genderGroup.getChildAt(radioId);
            gender = (String) btn.getText();
        }

        if (heightUnit.equals("cm")) {
            if (!isEmpty(heightCMValue)) {
                heightLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
                height = Integer.parseInt(heightCM.getText().toString());
                heightEntered = true;
            } else {
                heightEntered = false;
            }
        } else if (heightUnit.equals("ft")) {
            if (!isEmpty(heightFTValue) & !isEmpty(heightINValue)) {
                heightLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
                Integer feet = Integer.parseInt(heightFT.getText().toString());
                Integer inches = Integer.parseInt(heightIN.getText().toString());

                height = (int) ((feet * 30) + (inches * 2.54));
                heightEntered = true;
            } else {
                heightEntered = false;
            }
        }

        //If all the validation passes, submit the form. Else, show errors
        if (!isEmpty(name) & !isEmpty(age) & !isEmpty(job) & genderID != -1) {
            //Turn all errors off
            nameWrapper.setError(null);
            ageWrapper.setError(null);
            jobWrapper.setError(null);

            MainActivity.subCreated = true;

            //Hide the keyboard on click
            showKeyboard(false, mainActivity);

            //Enable additional menu items/fragments for recording and saving sensor data
            mainActivity.navigationView.getMenu().findItem(R.id.nav_start).setEnabled(true);
            mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setTitle("User Info");

            Snackbar.make(coordinatorLayout, "Subject created", Snackbar.LENGTH_SHORT).show();

            saveLogin(name, age, job, gender);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (position) {
            case 0:
                heightCM.setVisibility(View.VISIBLE);
                heightFT.setVisibility(View.INVISIBLE);
                heightIN.setVisibility(View.INVISIBLE);
                heightFTSym.setVisibility(View.INVISIBLE);
                heightINSym.setVisibility(View.INVISIBLE);
                heightUnit = "cm";

                //Only request focus if the user manually selected the spinner
                //Otherwise focus will be pulled on layout inflation
                if (MainActivity.heightUnitSpinnerTouched) {
                    heightCM.requestFocus();
                }
                break;
            case 1:
                heightCM.setVisibility(View.INVISIBLE);
                heightFT.setVisibility(View.VISIBLE);
                heightIN.setVisibility(View.VISIBLE);
                heightFTSym.setVisibility(View.VISIBLE);
                heightINSym.setVisibility(View.VISIBLE);
                heightUnit = "ft";

                //Only request focus if the user manually selected the spinner
                //Otherwise focus will be pulled on layout inflation
                if (MainActivity.heightUnitSpinnerTouched) {
                    heightFT.requestFocus();
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
