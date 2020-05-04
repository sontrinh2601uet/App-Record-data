package com.example.sensorrecord.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;

import static android.content.Context.MODE_PRIVATE;

public class UserInfoFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "SubjectInfoFragment";

    MainActivity mainActivity;
    Button deleteButton;
    TextView deleteMessage;
    ProgressDialog dialog;
    CoordinatorLayout coordinatorLayout;

    public UserInfoFragment() {
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
        mainActivity.setTitle("User Information");

        //Listener for delete button
        deleteButton = (Button) view.findViewById(R.id.subInfo_button_delete);
        deleteButton.setOnClickListener(this);
        deleteMessage = (TextView) view.findViewById(R.id.subInfo_delete_message);

        //Set state of delete button depending on whether recording is ongoing
        if (MainActivity.dataRecordStarted) {
            deleteButton.setEnabled(false);
            deleteMessage.setText(R.string.subInfo_message_recording);
        } else {
            deleteButton.setEnabled(true);
            deleteMessage.setText("");
        }

        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        //Get form text view elements
        TextView name = (TextView) view.findViewById(R.id.subInfo_value_name);
        TextView age = (TextView) view.findViewById(R.id.subInfo_value_age);
        TextView job = (TextView) view.findViewById(R.id.subInfo_value_job);
        TextView gender = (TextView) view.findViewById(R.id.subInfo_value_gender);
        TextView device = (TextView) view.findViewById(R.id.subInfo_value_device);

        //Set the text view elements in layout to subject info from temp table
        name.setText(pref.getString("name", null));
        age.setText(pref.getString("age", null));
        job.setText(pref.getString("job", null));
        gender.setText(pref.getString("gender", null));
        device.setText(pref.getString("device", null));

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        alertDialogBuilder.setTitle("Delete?");
        alertDialogBuilder.setMessage("Are you sure you want to delete the current user?\n This action is irreversible.");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Deleting a lot of sensor data might take a while, so use a background thread
                new DeleteSubjectTask().execute();
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog deleteAlertDialog = alertDialogBuilder.create();
        deleteAlertDialog.show();
    }

    //Async class for subject delete
    public class DeleteSubjectTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(mainActivity);
            dialog.setTitle("Delete subject");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            try {
                //Set subCreated flag to false
                MainActivity.subCreated = false;

                return true;
            } catch (SQLException e) {
                Snackbar.make(coordinatorLayout, "Error: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.snackbar_dismiss, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        }).show();

                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (success) {
                //Restart the main activity
                Snackbar.make(coordinatorLayout, "Subject deleted", Snackbar.LENGTH_SHORT).show();
                mainActivity.recreate();
            }
        }
    }

}
