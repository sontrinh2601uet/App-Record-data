package com.example.sensorrecord;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Environment;
import android.os.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVWriter;

public class ExportCSV {

    private static final String PATH_TO_INFORMATION = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SensorRecord" + File.separator + "information.csv";
    private boolean mHasInfor = false;

    private static String[] shareInfo = new String[5];

    public String getCurrentDateTime() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }

    public static void updateInfo() {

    }

    public static boolean isFirstLogin() {
        return true;
    }

    public void createInformation(String name, String job, short age, String sex, String version) {

    }

    public String getInfo(String type) throws SQLException {
        String result = "";

        if (c.getCount() > 0) {
            c.moveToFirst();
            switch (type) {
                case "date":
                    result = c.getString(c.getColumnIndex(SUBJECTS_DATE));
                    break;
                case "name":
                    result = c.getString(c.getColumnIndex(SUBJECTS_RESEARCH_J));
                    break;
                case "condition":
                    result = c.getString(c.getColumnIndex(SUBJECTS_CONDITION));
                    break;
                case "age":
                    result = c.getString(c.getColumnIndex(SUBJECTS_AGE));
                    break;
                case "sex":
                    result = c.getString(c.getColumnIndex(SUBJECTS_SEX));
                    break;
                case "height":
                    result = c.getString(c.getColumnIndex(SUBJECTS_HEIGHT));
                    break;
                case "time":
                    result = c.getString(c.getColumnIndex(SUBJECTS_START_TIME));
                    break;
            }
        }
        c.close();

        return result;
    }

    public void exportSubjectData(File outputFile, float[] acc,
                                  float[] gyro,
                                  float[] mag,
                                  float[] rot) throws IOException, SQLException {

        csvWrite = new CSVWriter(new FileWriter(outputFile));

        curCSV = db.rawQuery("SELECT * FROM " + DATA_TABLE_NAME + " WHERE id = " + subNum, null);

        csvWrite.writeNext(curCSV.getColumnNames());

        Integer writeCounter = 0;
        Integer numRows = curCSV.getCount();

        while (curCSV.moveToNext()) {
            writeCounter++;

            String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                    curCSV.getString(3), curCSV.getString(4), curCSV.getString(5),
                    curCSV.getString(6), curCSV.getString(7), curCSV.getString(8),
                    curCSV.getString(9), curCSV.getString(10), curCSV.getString(11),
                    curCSV.getString(12), curCSV.getString(13), curCSV.getString(14),
                    curCSV.getString(15), curCSV.getString(16), curCSV.getString(17),
                    curCSV.getString(18), curCSV.getString(19), curCSV.getString(20),
                    curCSV.getString(21), curCSV.getString(22)};

            csvWrite.writeNext(arrStr);

            if ((writeCounter % 1000) == 0) {
                csvWrite.flush();
            }

            Double progressPercent = Math.ceil(((float) writeCounter / (float) numRows) * 100);
            Message msg = Message.obtain();
            msg.obj = progressPercent;
            msg.setTarget(messageHandler);
            msg.sendToTarget();
        }

        csvWrite.close();
        curCSV.close();
    }
}
