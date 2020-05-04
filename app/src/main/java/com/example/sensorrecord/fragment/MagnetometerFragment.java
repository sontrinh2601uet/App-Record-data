package com.example.sensorrecord.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;
import com.example.sensorrecord.library.DynamicLinePlot;

public class MagnetometerFragment extends Fragment implements SensorEventListener {

    SensorManager sensorManager;
    Sensor sensor;
    Sensor magnetometer;
    Handler handler;
    Runnable runnable;
    TextView textViewXAxis;
    TextView textViewYAxis;
    TextView textViewZAxis;
    RadioButton magCalibrated;
    RadioButton magUncalibrated;

    float[] magData;

    XYPlot plot;
    DynamicLinePlot dynamicPlot;

    public MagnetometerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_magnetometer, container, false);

        //Set the nav drawer item highlight
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_magnetometer);

        //Set actionbar title
        mainActivity.setTitle("Magnetometer");

        //Get text views
        textViewXAxis = (TextView) view.findViewById(R.id.value_x_axis);
        textViewYAxis = (TextView) view.findViewById(R.id.value_y_axis);
        textViewZAxis = (TextView) view.findViewById(R.id.value_z_axis);

        //Get radio buttons
        magCalibrated = (RadioButton) view.findViewById(R.id.gyro_select_calibrated);
        magUncalibrated = (RadioButton) view.findViewById(R.id.gyro_select_uncalibrated);


        //Sensor manager
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(MainActivity.TYPE_MAGNETIC);

        //Create graph
        magData = new float[3];

        plot = (XYPlot) view.findViewById(R.id.plot_sensor);
        dynamicPlot = new DynamicLinePlot(plot, getContext(), "Rotation (rad/sec)");
        dynamicPlot.setMaxRange(10);
        dynamicPlot.setMinRange(-10);
        dynamicPlot.addSeriesPlot("X", 0, ContextCompat.getColor(getContext(), R.color.graphX));
        dynamicPlot.addSeriesPlot("Y", 1, ContextCompat.getColor(getContext(), R.color.graphY));
        dynamicPlot.addSeriesPlot("Z", 2, ContextCompat.getColor(getContext(), R.color.graphZ));

        //Handler for graph plotting on background thread
        handler = new Handler();

        //Runnable for background plotting
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 10);
                plotData();
                updateGyroText();
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED), SensorManager.SENSOR_DELAY_FASTEST);

        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;

        int i = sensor.getType();

        if (magCalibrated.isChecked() & i == MainActivity.TYPE_MAGNETIC) {
            magData = event.values;
        } else if (magUncalibrated.isChecked() & i == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
            magData = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Safe not to implement
    }

    private void plotData() {
        dynamicPlot.setData(magData[0], 0);
        dynamicPlot.setData(magData[1], 1);
        dynamicPlot.setData(magData[2], 2);

        dynamicPlot.draw();
    }

    protected void updateGyroText() {
        // Update the gyroscope data
        textViewXAxis.setText(String.format("%.2f", magData[0]));
        textViewYAxis.setText(String.format("%.2f", magData[1]));
        textViewZAxis.setText(String.format("%.2f", magData[2]));
    }

}
