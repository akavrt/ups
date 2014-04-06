package com.akavrt.worko.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class DataLogger {
    private static final String TAG = DataLogger.class.getName();
    private static final String FILE_NAME_FORMAT = "data_%s.txt";
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd-HH-mm-ss";
    private static final DecimalFormat DECIMAL_FORMAT;
    private static DateFormat DATE_FORMAT;

    static {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        formatSymbols.setDecimalSeparator('.');
        formatSymbols.setGroupingSeparator(',');

        DECIMAL_FORMAT = new DecimalFormat();
        DECIMAL_FORMAT.setDecimalFormatSymbols(formatSymbols);
        DECIMAL_FORMAT.setGroupingUsed(false);
        DECIMAL_FORMAT.setMinimumFractionDigits(3);
        DECIMAL_FORMAT.setMaximumFractionDigits(3);

        DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
    }

    private final File path;
    private PrintWriter writer;

    public DataLogger(Context context) {
        path = context.getExternalFilesDir(null);
    }

    public void start() {
        String fileName = String.format(FILE_NAME_FORMAT, DATE_FORMAT.format(new Date()));

        File file = new File(path, fileName);
        try {
            writer = new PrintWriter(file, "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Can't create file: " + file.getPath());
        }
    }

    public void log(String message) {
        if (writer == null) {
            return;
        }

        writer.println(message);
    }

    public void stop() {
        if (writer == null) {
            return;
        }

        writer.close();
        writer = null;
    }

    public static String formatFloat(float value) {
        return DECIMAL_FORMAT.format(value);
    }
}
