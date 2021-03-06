package com.example.sensorrecord.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Build;
import android.os.Environment;

import com.example.sensorrecord.fragment.RecordFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class DataConnection {
    private static final String TAG = "DATA CONNECTION";
    private static final String ACCELEROMETER = "Accelerometer";
    private static final String GYROSCOPE = "Gyroscope";
    private static final String MAGNETIC = "Magnetometer";
    private static final String GRAVITY = "Gravity";
    private static final String ORIENTATION = "Orientation";

    private String headerFile;
    static ArrayList<float[]> timeLineData = new ArrayList<>();

    private SharedPreferences pref;
    private SimpleDateFormat sdf;
    private Context context;

    public DataConnection(Context context) {
        pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        this.context = context;
    }

    public void insertDataTemp(long time, float[] acc, float[] gyro, float[] mag, float[] gra, float[] ori) {

        float[] data = new float[16];

        data[0] = time;

        data[1] = acc[0];
        data[2] = acc[1];
        data[3] = acc[2];

        data[4] = gyro[0];
        data[5] = gyro[1];
        data[6] = gyro[2];

        data[7] = mag[0];
        data[8] = mag[1];
        data[9] = mag[2];

        data[10] = gra[0];
        data[11] = gra[1];
        data[12] = gra[2];

        data[13] = ori[0];
        data[14] = ori[1];
        data[14] = ori[2];

        timeLineData.add(data);
    }

    public void exportTrackingData(int trialTimes) throws SQLException {
        sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String actionTemplate = RecordFragment.currentTemplateAction;
        String typeSensor = RecordFragment.currentTypeSensor;
        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File exportDir = new File(pathToExternalStorage, "/SensorRecord");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File folderData = new File(exportDir, File.separator + "record");
        folderData.mkdir();

        getHeaderFile(actionTemplate, typeSensor);

        String name = actionTemplate.substring(0, actionTemplate.indexOf("-")) + "_"
                + typeSensor.toLowerCase()  + "_"
                + pref.getInt("id", 1) + "_"
                + trialTimes;

        exportData(null, name, folderData);
    }

    private void exportData(String sensor, String name, File folderData) {
        File accData = new File(folderData, name + ".txt");
        int startPos = 0;
        String typeSensor = RecordFragment.currentTypeSensor;

        switch (typeSensor) {
            case ACCELEROMETER:
                startPos = 1;
                break;
            case GYROSCOPE:
                startPos = 4;
                break;
            case MAGNETIC:
                startPos = 7;
                break;
            case GRAVITY:
                startPos = 10;
                break;
            case ORIENTATION:
                startPos = 13;
                break;
        }
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(accData), StandardCharsets.UTF_8);

            writer.write(headerFile);

            for (float[] timeLineDatum : timeLineData) {
                writer.write(new BigDecimal(timeLineDatum[0]).toPlainString() + ","
                        + timeLineDatum[startPos] + ","
                        + timeLineDatum[startPos + 1] + ","
                        + timeLineDatum[startPos + 2] + "\n");
            }

            writer.close();
        } catch (IOException e) {
        }

        clearRecordData();
    }

    private void getHeaderFile(String actionTemplate, String sensor) {
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

        StringBuilder header = new StringBuilder();

        header.append("SubjectId: " + pref.getInt("id", 1) + "\n");
        header.append("Name: " + pref.getString("name", null) + "\n");
        header.append("Age: " + pref.getString("age", null) + "\n");
        header.append("Job: " + pref.getString("job", null) + "\n");
        header.append("Device version: " + Build.VERSION.SDK_INT + "\n");
        header.append("Gender: " + pref.getString("gender", null) + "\n");
        header.append("Height: " + pref.getInt("height", 0) + " " + pref.getString("heightUnit", null) + "\n");
        header.append("Template action: " + actionTemplate + "\n");
        header.append("Sensor type record: " + sensor + "\n");
        header.append("Time: " + sdf.format(new Date()) + "\n");
        header.append("\n================================\n\n");
        header.append("@DATA\n");

        headerFile = header.toString();
    }

    private void clearRecordData() {
        timeLineData = new ArrayList<>();
    }
}
