package com.example.sensorrecord;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SubjectInfoFragment extends Fragment {

    public static final String TAG = "SubjectInfoFragment";

    MainActivity mainActivity;
    DBHelper dbHelper;
    ProgressDialog dialog;
    CoordinatorLayout coordinatorLayout;

    public SubjectInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subject_info, container, false);

        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_new);

        //Set actionbar title
        mainActivity.setTitle("Subject Information");

        //DBHelper
        dbHelper = DBHelper.getInstance(getActivity());

        //Get form text view elements
        TextView date = (TextView) view.findViewById(R.id.subInfo_value_date);
        TextView name = (TextView) view.findViewById(R.id.subInfo_label_name);
        TextView job = (TextView) view.findViewById(R.id.subInfo_label_job);
        TextView age = (TextView) view.findViewById(R.id.subInfo_value_age);
        TextView sex = (TextView) view.findViewById(R.id.subInfo_value_sex);
        TextView version = (TextView) view.findViewById(R.id.subInfo_value_device_version);

        //Set the text view elements in layout to subject info from temp table
        date.setText(dbHelper.getTempSubInfo("date"));
        name.setText(dbHelper.getTempSubInfo("name"));
        job.setText(dbHelper.getTempSubInfo("job"));
        age.setText(dbHelper.getTempSubInfo("age"));
        sex.setText(dbHelper.getTempSubInfo("sex"));
        version.setText(dbHelper.getTempSubInfo("version"));

        // Inflate the layout for this fragment
        return view;
    }
}
