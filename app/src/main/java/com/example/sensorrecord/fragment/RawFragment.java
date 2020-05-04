package com.example.sensorrecord.fragment;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;

public class RawFragment extends Fragment implements SensorEventListener {
    final short POLL_FREQUENCY = 200; //in milliseconds
    Sensor sensor;
    MainActivity mainActivity;
    TextView accX;
    TextView accY;
    TextView accZ;
    TextView magX;
    TextView magY;
    TextView magZ;
    TextView gyroX;
    TextView gyroY;
    TextView gyroZ;
    TextView longitude;
    TextView latitude;
    float[] accelerometerMatrix = new float[3];
    float[] gyroscopeMatrix = new float[3];
    float[] magneticMatrix = new float[3];
    float[] GpsMatrix = new float[2];
    private long lastUpdate = -1;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetic;

    public RawFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_raw, container, false);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_raw);

        //Set actionbar title
        mainActivity.setTitle("Raw Data");

        //Sensor manager
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(MainActivity.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(MainActivity.TYPE_GYROSCOPE);
        magnetic = sensorManager.getDefaultSensor(MainActivity.TYPE_MAGNETIC);

        //Get text fields
        accX = (TextView) view.findViewById(R.id.raw_value_acc_x);
        accY = (TextView) view.findViewById(R.id.raw_value_acc_y);
        accZ = (TextView) view.findViewById(R.id.raw_value_acc_z);

        magX = (TextView) view.findViewById(R.id.raw_value_mag_x);
        magY = (TextView) view.findViewById(R.id.raw_value_mag_y);
        magZ = (TextView) view.findViewById(R.id.raw_value_mag_z);

        gyroX = (TextView) view.findViewById(R.id.raw_value_gyro_x);
        gyroY = (TextView) view.findViewById(R.id.raw_value_gyro_y);
        gyroZ = (TextView) view.findViewById(R.id.raw_value_gyro_z);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;

        int i = sensor.getType();

        if (i == MainActivity.TYPE_ACCELEROMETER) {
            accelerometerMatrix = event.values;
        } else if (i == MainActivity.TYPE_GYROSCOPE) {
            gyroscopeMatrix = event.values;
        } else if (i == MainActivity.TYPE_MAGNETIC) {
            magneticMatrix = event.values;
        }

        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - lastUpdate);

        // only allow one update every POLL_FREQUENCY.
        if (diffTime > POLL_FREQUENCY) {
            lastUpdate = curTime;

            SensorManager.getRotationMatrix(accelerometerMatrix, null, gyroscopeMatrix, magneticMatrix);

            accX.setText(String.format("%.2f", accelerometerMatrix[0]));
            accY.setText(String.format("%.2f", accelerometerMatrix[1]));
            accZ.setText(String.format("%.2f", accelerometerMatrix[2]));

            magX.setText(String.format("%.2f", magneticMatrix[0]));
            magY.setText(String.format("%.2f", magneticMatrix[1]));
            magZ.setText(String.format("%.2f", magneticMatrix[2]));

            gyroX.setText(String.format("%.2f", gyroscopeMatrix[0]));
            gyroY.setText(String.format("%.2f", gyroscopeMatrix[1]));
            gyroZ.setText(String.format("%.2f", gyroscopeMatrix[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //safe not to implement
    }
}
