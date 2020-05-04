package com.example.sensorrecord.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
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
    private static final String ACCELEROMETER = "accelerometer";
    private static final String GYROSCOPE = "gyroscope";
    private static final String MAGNETIC = "magnetic";
    private static final String GRAVITY = "gravity";
    private static final String GPS = "gps";

    private String headerFile;
    static ArrayList<float[]> timeLineData = new ArrayList<>();

    private SimpleDateFormat sdf;
    private Context context;

    public DataConnection(Context context) {
        this.context = context;
    }

    public void insertDataTemp(long time, float[] acc, float[] gyro, float[] mag, float[] gra, float[] gps) {

        float[] data = new float[15];

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

        data[13] = gps[0];
        data[14] = gps[1];

        timeLineData.add(data);
    }

    public void exportTrackingData() throws SQLException {
        sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String actionTemplate = RecordFragment.currentTemplateAction;
        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File exportDir = new File(pathToExternalStorage, "/SensorRecord");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File folderData = new File(exportDir, File.separator + sdf.format(new Date()) + "_" + actionTemplate);
        folderData.mkdir();

        getHeaderFile(actionTemplate);
        exportData(ACCELEROMETER, folderData);
        exportData(GYROSCOPE, folderData);
        exportData(MAGNETIC, folderData);
        exportData(GRAVITY, folderData);
    }

    private void exportData(String name, File folderData) {
        File accData = new File(folderData, name + ".txt");
        int startPos = 0;

        switch (name) {
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
    }

    private void getHeaderFile(String actionTemplate) {
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);

        StringBuilder header = new StringBuilder();

        header.append("Record Sensor Data Application\n");
        header.append("Name: " + pref.getString("name", null) + "\n");
        header.append("Age: " + pref.getString("age", null) + "\n");
        header.append("Job: " + pref.getString("job", null) + "\n");
        header.append("Device version: " + pref.getString("device", null) + "\n");
        header.append("Gender: " + pref.getString("gender", null) + "\n");
        header.append("Template: " + actionTemplate + "\n");
        header.append("Time: " + timeLineData.get(0)[0] + "\n");
        header.append("\n================================\n\n");

        headerFile = header.toString();
    }
}
