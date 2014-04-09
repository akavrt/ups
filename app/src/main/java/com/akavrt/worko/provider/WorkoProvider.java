package com.akavrt.worko.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import static com.akavrt.worko.provider.WorkoContract.AllTimeStat;
import static com.akavrt.worko.provider.WorkoContract.CONTENT_AUTHORITY;
import static com.akavrt.worko.provider.WorkoContract.MonthStat;
import static com.akavrt.worko.provider.WorkoContract.PATH_MONTH;
import static com.akavrt.worko.provider.WorkoContract.PATH_SETS;
import static com.akavrt.worko.provider.WorkoContract.PATH_STAT;
import static com.akavrt.worko.provider.WorkoContract.PATH_WEEK;
import static com.akavrt.worko.provider.WorkoContract.Sets;
import static com.akavrt.worko.provider.WorkoContract.StatisticsColumns;
import static com.akavrt.worko.provider.WorkoContract.WeekStat;
import static com.akavrt.worko.provider.WorkoDatabase.Tables;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class WorkoProvider extends ContentProvider {
    private static final String TAG = WorkoProvider.class.getName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int SETS = 100;
    private static final int SETS_ID = 101;

    private static final int STAT = 200;
    private static final int STAT_WEEK = 300;
    private static final int STAT_MONTH = 400;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, PATH_SETS, SETS);
        matcher.addURI(authority, PATH_SETS + "/#", SETS_ID);

        matcher.addURI(authority, PATH_STAT, STAT);
        matcher.addURI(authority, PATH_STAT + "/" + PATH_WEEK, STAT_WEEK);
        matcher.addURI(authority, PATH_STAT + "/" + PATH_MONTH, STAT_MONTH);

        return matcher;
    }

    static String STAT_SQL_UNBOUNDED =
            "SELECT " +
                    "count(distinct " + Sets.DAY + ") as " + StatisticsColumns.DAYS + ", " +
                    "count(" + Sets._ID + ") as " + StatisticsColumns.SETS + ", " +
                    "sum(" + Sets.PULL_UPS + ") as " + StatisticsColumns.PULL_UPS + ", " +
                    "max(" + Sets.PULL_UPS + ") as " + StatisticsColumns.RECORD + " " +
                    "FROM " +
                    Tables.SETS;

    private WorkoDatabase mOpenHelper;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new WorkoDatabase(context);

        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SETS:
                return Sets.CONTENT_TYPE;

            case SETS_ID:
                return Sets.CONTENT_ITEM_TYPE;

            case STAT:
                return AllTimeStat.CONTENT_TYPE;

            case STAT_WEEK:
                return WeekStat.CONTENT_TYPE;

            case STAT_MONTH:
                return MonthStat.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);

        Cursor result;
        switch (match) {
            case SETS:
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(Tables.SETS);
                result = builder.query(
                        db, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case STAT:
                String sql = STAT_SQL_UNBOUNDED + " ORDER BY " + BaseColumns._ID + " ASC";
                result = db.rawQuery(sql, null);
                break;

            case STAT_WEEK:
                result = new IntervalStatHelper(getContext(), db).weekStat();
                break;

            case STAT_MONTH:
                result = new IntervalStatHelper(getContext(), db).monthStat();
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (result != null) {
            // The only table which contains real data is Sets table
            // That's why we should watch same URI for all queries.
            result.setNotificationUri(getContext().getContentResolver(), Sets.CONTENT_URI);
        }

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        final ContentResolver resolver = getContext().getContentResolver();

        long rowId;
        switch (match) {
            case SETS:
                rowId = db.insertOrThrow(Tables.SETS, null, values);
                Uri newUri = ContentUris.withAppendedId(Sets.CONTENT_URI, rowId);
                resolver.notifyChange(Sets.CONTENT_URI, null);

                return newUri;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete(uri=" + uri + ")");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int count;

        switch (match) {
            case SETS:
                count = db.delete(Tables.SETS, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Operation doesn't supported or unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

}
