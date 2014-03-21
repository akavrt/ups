package com.akavrt.ups.utils;

import android.os.Environment;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class FileUtils {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }
}
