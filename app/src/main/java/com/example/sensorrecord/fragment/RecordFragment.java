package com.example.sensorrecord.fragment;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;
import com.example.sensorrecord.library.DataConnection;
import com.example.sensorrecord.library.SensorService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class RecordFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "RecordFragment";
    private static final int MIN_FREQUENCY = 1;
    private static final int MAX_FREQUENCY = 100;
    private static final int MIN_TIME_RECORD = 1;
    private static final int MAX_TIME_RECORD = 300;
    //Message handler for service
    public static String currentTemplateAction;
    public static Handler messageHandler = new MessageHandler();

    static MainActivity mainActivity;
    static ProgressDialog stopDialog;
    Button startButton;
    CoordinatorLayout coordinatorLayout;
    TextView recordProgressMessage;
    EditText frequency;
    EditText timeRecord;
    RadioGroup group;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_record, container, false);

        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_start);

        //Set actionbar title
        mainActivity.setTitle("Recording Data");

        //Get form text view element and set
        createTemplateActionView(view);
        recordProgressMessage = (TextView) view.findViewById(R.id.start_recording_progress);
        frequency = (EditText) view.findViewById(R.id.frequency_value);
        timeRecord = (EditText) view.findViewById(R.id.time_record);
        startButton = (Button) view.findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        frequency.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = frequency.getText().toString();
                if (!text.equals("")) {
                    int frequencyValue = Integer.parseInt(text);
                    if (frequencyValue < MIN_TIME_RECORD || frequencyValue > MAX_TIME_RECORD) {
                        frequency.setError("Out of Range Value (1 ~ 300 second)");
                        startButton.setEnabled(false);
                    } else {
                        startButton.setEnabled(true);
                    }
                }
            }
        });

        timeRecord.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = timeRecord.getText().toString();
                if (!text.equals("")) {
                    int frequencyValue = Integer.parseInt(text);
                    if (frequencyValue < MIN_FREQUENCY || frequencyValue > MAX_FREQUENCY) {
                        frequency.setError("Out of Range Value (1 ~ 100 Hz)");
                        startButton.setEnabled(false);
                    } else {
                        startButton.setEnabled(true);
                    }
                }
            }
        });

        //Set button state depending on whether recording has been started and/or stopped
        if (MainActivity.dataRecordStarted) {
            //started and not completed: enable STOP button
            startButton.setText(R.string.start_button_label_stop);
        } else {
            //Haven't started: enable START bu
            startButton.setText(R.string.start_button_label_start);
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void createTemplateActionView(View view) {
        group = (RadioGroup) view.findViewById(R.id.template_of_action_button);

        String sampleData = getString(R.string.sample_data);

        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File exportDir = new File(pathToExternalStorage, "/SensorRecord");
        File templateAction = new File(exportDir, "templateAction.txt");

        if (!templateAction.isFile()) {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(templateAction), StandardCharsets.UTF_8);
                writer.write(sampleData);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StringBuilder dataInFile = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(templateAction), StandardCharsets.UTF_8));
            String str;

            while ((str = in.readLine()) != null) {
                dataInFile.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] listTemplate = dataInFile.toString().split(",");

        for (int i = 0; i < listTemplate.length; i++) {
            RadioButton template = new RadioButton(getContext());

            template.setText(listTemplate[i]);
            template.setId(i);
            group.addView(template);
        }

        group.check(0);
        currentTemplateAction = ((RadioButton) group.getChildAt(0)).getText().toString();
        group.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected
            RadioButton button = (RadioButton) group.findViewById(checkedId);
            currentTemplateAction = button.getText().toString();
        });
    }

    @Override
    public void onClick(View v) {

        if (!MainActivity.dataRecordStarted) {
            try {
                setAutoStop();
                //Disable the hamburger, and swipes, while recording
                mainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mainActivity.hamburger.setDrawerIndicatorEnabled(false);
                mainActivity.hamburger.setHomeAsUpIndicator(new DrawerArrowDrawable(getActivity()));
                mainActivity.hamburger.syncState();

                //Set recording progress message
                recordProgressMessage.setText(R.string.start_recording_progress);
                MainActivity.dataRecordStarted = true;
                startButton.setText(R.string.start_button_label_stop);

                //Start the service
                Intent startService = new Intent(mainActivity, SensorService.class);
                startService.putExtra("messenger", new Messenger(messageHandler));
                startService.putExtra("frequency", Short.valueOf(frequency.getText().toString()));
                getContext().startService(startService);

                Snackbar.make(coordinatorLayout, R.string.start_recording, Snackbar.LENGTH_SHORT).show();
                startButton.setEnabled(false);
            } catch (SQLException e) {
            }
        } else {
            recordProgressMessage.setText("");
            MainActivity.dataRecordStarted = false;
            startButton.setText(R.string.start_button_label_start);

            //Stop the service
            mainActivity.stopService(new Intent(mainActivity, SensorService.class));

            //Re-enable the hamburger, and swipes, after recording
            mainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mainActivity.hamburger.setDrawerIndicatorEnabled(true);
            mainActivity.hamburger.syncState();

            DataConnection connection = new DataConnection(getContext());
            connection.exportTrackingData();

            //Show snackbar message for recording complete
            Snackbar.make(coordinatorLayout, R.string.start_recording_complete, Snackbar.LENGTH_SHORT).show();
            startButton.setEnabled(true);
        }
    }

    private void setAutoStop() {
        int time = Integer.parseInt(timeRecord.getText().toString()) * 1000;

        new Handler().postDelayed(() -> {
            startButton.performClick();
            Vibrator v = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }, time);
    }

    public static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            int state = message.arg1;
            switch (state) {
                case 0:
                    //Dismiss dialog
                    stopDialog.dismiss();
                    Log.d(TAG, "Stop dialog dismissed");
                    break;

                case 1:
                    //Show stop dialog
                    stopDialog = new ProgressDialog(mainActivity);
                    stopDialog.setTitle("Stopping sensors");
                    stopDialog.setMessage("Please wait...");
                    stopDialog.setProgressNumberFormat(null);
                    stopDialog.setCancelable(false);
                    stopDialog.setMax(100);
                    stopDialog.show();
                    Log.d(TAG, "Stop dialog displayed");
                    break;
            }
        }
    }
}
