package com.example.sensorrecord.library;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.example.sensorrecord.MainActivity;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SensorService extends Service implements SensorEventListener {

    public static final String TAG = "SensorService";

    public static int SCREEN_OFF_RECEIVER_DELAY = 100;

    //This can't be set below 10ms due to Android/hardware limitations. Use 9 to get more accurate 10ms intervals
    private short POLL_FREQUENCY; //in milliseconds
    long curTime;
    ExecutorService executor;
    DataConnection dataConnection;
    Sensor sensor;
    Sensor accelerometer;
    Sensor gyroscope;
    Sensor gravity;
    Sensor magnetic;
    Sensor orientation;
    float[] accelerometerMatrix = new float[3];
    float[] gyroscopeMatrix = new float[3];
    float[] gravityMatrix = new float[3];
    float[] magneticMatrix = new float[3];
    float[] orientationMatrix = new float[3];
    private long lastUpdate = -1;
    private Messenger messageHandler;
    private SensorManager sensorManager = null;

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive(" + intent + ")");

            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    Log.i(TAG, "Runnable executing...");
                    unregisterListener();
                    registerListener();
                }
            };

            new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
        }
    };
    private WakeLock wakeLock = null;

    private void registerListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    public void sendMessage(String state) {
        Message message = Message.obtain();
        switch (state) {
            case "HIDE":
                message.arg1 = 0;
                break;
            case "SHOW":
                message.arg1 = 1;
                break;
        }
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Safe not to implement
    }

    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;

        //Store sensor data
        int i = sensor.getType();
        if (i == MainActivity.TYPE_ACCELEROMETER) {
            accelerometerMatrix = event.values;
        } else if (i == MainActivity.TYPE_GYROSCOPE) {
            gyroscopeMatrix = event.values;
        } else if (i == MainActivity.TYPE_MAGNETIC) {
            magneticMatrix = event.values;
        } else if (i == MainActivity.TYPE_GRAVITY) {
            magneticMatrix = event.values;
        } else if (i == MainActivity.TYPE_ORIENTATION) {
            orientationMatrix = event.values;
        }

        curTime = event.timestamp; //in nanoseconds

        // only allow one update every POLL_FREQUENCY (convert from ms to nano for comparison).
        if ((curTime - lastUpdate) > POLL_FREQUENCY * 1000000) {

            lastUpdate = curTime;

            try {
                Runnable insertHandler = new InsertHandler(curTime, accelerometerMatrix, gyroscopeMatrix,
                        gravityMatrix, magneticMatrix, orientationMatrix);
                executor.execute(insertHandler);
            } catch (SQLException e) {
                Log.e(TAG, "insertData: " + e.getMessage(), e);
            }
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();

        dataConnection = new DataConnection(getApplicationContext());
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(MainActivity.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(MainActivity.TYPE_GYROSCOPE);
        magnetic = sensorManager.getDefaultSensor(MainActivity.TYPE_MAGNETIC);
        orientation = sensorManager.getDefaultSensor(MainActivity.TYPE_ORIENTATION);

        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        //Executor service for DB inserts
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startForeground(Process.myPid(), new Notification());
        registerListener();
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        //Message handler for progress dialog
        Bundle extras = intent.getExtras();
        messageHandler = (Messenger) extras.get("messenger");
        POLL_FREQUENCY = (short) (1000 / extras.getShort("frequency"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Show dialog
        sendMessage("SHOW");

        //Unregister receiver and listener prior to executor shutdown
        unregisterReceiver(receiver);
        unregisterListener();

        //Prevent new tasks from being added to thread
        executor.shutdown();
        Log.d(TAG, "Executor shutdown is called");

        //Create new thread to wait for executor to clear queue and wait for termination
        new Thread(new Runnable() {

            public void run() {
                try {
                    //Wait for all tasks to finish before we proceed
                    while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        Log.i(TAG, "Waiting for current tasks to finish");
                    }
                    Log.i(TAG, "No queue to clear");
                } catch (InterruptedException e) {
                    Log.e(TAG, "Exception caught while waiting for finishing executor tasks");
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                if (executor.isTerminated()) {
                    //Stop everything else once the task queue is clear
                    wakeLock.release();
                    stopForeground(true);

                    //Dismiss progress dialog
                    sendMessage("HIDE");
                }
            }
        }).start();
    }

    class InsertHandler implements Runnable {
        final long curTime;
        final float[] accelerometerMatrix;
        final float[] gyroscopeMatrix;
        final float[] magneticMatrix;
        final float[] gravityMatrix;
        final float[] orientationMatrix;

        //Store the current sensor array values into THIS objects arrays, and db insert from this object
        InsertHandler(long curTime, float[] accelerometerMatrix,
                      float[] gyroscopeMatrix, float[] gravityMatrix,
                      float[] magneticMatrix, float[] orientationMatrix) {

            this.curTime = curTime;
            this.accelerometerMatrix = accelerometerMatrix;
            this.gyroscopeMatrix = gyroscopeMatrix;
            this.magneticMatrix = magneticMatrix;
            this.gravityMatrix = gravityMatrix;
            this.orientationMatrix = orientationMatrix;
        }

        public void run() {
            Log.i("", new BigDecimal(this.curTime / 1000000).toPlainString());
            dataConnection.insertDataTemp(
                    this.curTime / 1000000,
                    this.accelerometerMatrix,
                    this.gyroscopeMatrix,
                    this.magneticMatrix,
                    this.gravityMatrix,
                    this.orientationMatrix);
        }
    }
}