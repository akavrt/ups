package com.akavrt.worko.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class WorkoDatabase extends SQLiteOpenHelper {
    private static final String TAG = WorkoDatabase.class.getName();

    private static final String DATABASE_NAME = "worko.db";

    private static final int DATABASE_VERSION = 2;

    public interface Tables {
        String SETS = "sets";
    }

    public WorkoDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.SETS + " ("
                + WorkoContract.Sets._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WorkoContract.Sets.PULL_UPS + " INTEGER NOT NULL,"
                + WorkoContract.Sets.DAY + " INTEGER NOT NULL"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + Tables.SETS);

        onCreate(db);
    }
}
