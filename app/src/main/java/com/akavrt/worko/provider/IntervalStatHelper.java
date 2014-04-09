package com.akavrt.worko.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import java.util.Locale;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class IntervalStatHelper {
    private static String sStatSqlBounded = WorkoProvider.STAT_SQL_UNBOUNDED +
            " WHERE " + WorkoContract.Sets.DAY + " >= ? AND " + WorkoContract.Sets.DAY + " < ?";

    private final Context mContext;
    private final SQLiteDatabase mDb;

    public IntervalStatHelper(Context context, SQLiteDatabase db) {
        this.mContext = context;
        this.mDb = db;
    }

    public Cursor weekStat() {
        Locale currentLocale = mContext.getResources().getConfiguration().locale;

        Calendar currentDate = Calendar.getInstance(currentLocale);
        int firstDayOfWeek = currentDate.getFirstDayOfWeek();
        int days = (currentDate.get(Calendar.DAY_OF_WEEK) + 7 - firstDayOfWeek) % 7;

        Calendar currWeekStartDate = Calendar.getInstance(currentLocale);
        currWeekStartDate.setTime(currentDate.getTime());
        currWeekStartDate.add(Calendar.DATE, -days);
        currWeekStartDate.set(Calendar.HOUR_OF_DAY, 0);
        currWeekStartDate.set(Calendar.MINUTE, 0);
        currWeekStartDate.set(Calendar.SECOND, 0);
        currWeekStartDate.set(Calendar.MILLISECOND, 0);

        Calendar prevWeekStartDate = Calendar.getInstance(currentLocale);
        prevWeekStartDate.setTime(currWeekStartDate.getTime());
        prevWeekStartDate.add(Calendar.DATE, -7);

        Calendar nextWeekStartDate = Calendar.getInstance(currentLocale);
        nextWeekStartDate.setTime(currWeekStartDate.getTime());
        nextWeekStartDate.add(Calendar.DATE, 7);

        return adjacentIntervalsStat(
                currentLocale,
                prevWeekStartDate,
                currWeekStartDate,
                nextWeekStartDate);
    }

    public Cursor monthStat() {
        Locale currentLocale = mContext.getResources().getConfiguration().locale;

        Calendar currMonthStartDate = Calendar.getInstance(currentLocale);
        currMonthStartDate.set(Calendar.DAY_OF_MONTH,
                currMonthStartDate.getActualMinimum(Calendar.DAY_OF_MONTH));
        currMonthStartDate.set(Calendar.HOUR_OF_DAY, 0);
        currMonthStartDate.set(Calendar.MINUTE, 0);
        currMonthStartDate.set(Calendar.SECOND, 0);
        currMonthStartDate.set(Calendar.MILLISECOND, 0);

        Calendar prevMonthStartDate = Calendar.getInstance(currentLocale);
        prevMonthStartDate.setTime(currMonthStartDate.getTime());
        prevMonthStartDate.add(Calendar.MONTH, -1);

        Calendar nextMonthStartDate = Calendar.getInstance(currentLocale);
        nextMonthStartDate.setTime(currMonthStartDate.getTime());
        nextMonthStartDate.add(Calendar.MONTH, 1);

        return adjacentIntervalsStat(
                currentLocale,
                prevMonthStartDate,
                currMonthStartDate,
                nextMonthStartDate);
    }

    private Cursor adjacentIntervalsStat(Locale locale,
                                         Calendar prevIntervalStartDate,
                                         Calendar currIntervalStartDate,
                                         Calendar nextIntervalStartDate) {
        String prevIntervalStartDateSt =
                Long.toString(prevIntervalStartDate.getTimeInMillis() / 1000);
        String currIntervalStartDateSt =
                Long.toString(currIntervalStartDate.getTimeInMillis() / 1000);
        String nextIntervalStartDateSt =
                Long.toString(nextIntervalStartDate.getTimeInMillis() / 1000);

        MatrixCursor result = new MatrixCursor(
                new String[]{
                        WorkoContract.WeekStat.DAYS,
                        WorkoContract.WeekStat.SETS,
                        WorkoContract.WeekStat.PULL_UPS,
                        WorkoContract.WeekStat.RECORD,
                        WorkoContract.WeekStat.INTERVAL_START,
                        WorkoContract.WeekStat.INTERVAL_END
                }
        );


        // current interval
        MatrixCursor.RowBuilder builder =
                addIntervalStat(currIntervalStartDateSt, nextIntervalStartDateSt, result);

        Calendar currIntervalEndDate = Calendar.getInstance(locale);
        currIntervalEndDate.setTime(nextIntervalStartDate.getTime());
        currIntervalEndDate.add(Calendar.DATE, -1);

        builder.add(currIntervalStartDate.getTimeInMillis());
        builder.add(currIntervalEndDate.getTimeInMillis());

        // previous interval
        builder = addIntervalStat(prevIntervalStartDateSt, currIntervalStartDateSt, result);

        Calendar prevIntervalEndDate = Calendar.getInstance(locale);
        prevIntervalEndDate.setTime(currIntervalStartDate.getTime());
        prevIntervalEndDate.add(Calendar.DATE, -1);

        builder.add(prevIntervalStartDate.getTimeInMillis());
        builder.add(prevIntervalEndDate.getTimeInMillis());

        return result;
    }

    private MatrixCursor.RowBuilder addIntervalStat(String currIntervalStartDateSt,
                                                    String nextIntervalStartDateSt,
                                                    MatrixCursor result) {
        Cursor interval = mDb.rawQuery(sStatSqlBounded,
                new String[]{currIntervalStartDateSt, nextIntervalStartDateSt});

        int daysIndex = interval.getColumnIndexOrThrow(WorkoContract.StatisticsColumns.DAYS);
        int setsIndex = interval.getColumnIndexOrThrow(WorkoContract.StatisticsColumns.SETS);
        int pullUpsIndex = interval.getColumnIndexOrThrow(WorkoContract.StatisticsColumns.PULL_UPS);
        int recordIndex = interval.getColumnIndexOrThrow(WorkoContract.StatisticsColumns.RECORD);

        int intervalDays = 0;
        int intervalSets = 0;
        int intervalPullUps = 0;
        int intervalRecord = 0;
        if (interval != null) {
            if (interval.getCount() > 0 && interval.moveToNext()) {
                intervalDays = interval.getInt(daysIndex);
                intervalSets = interval.getInt(setsIndex);
                intervalPullUps = interval.getInt(pullUpsIndex);
                intervalRecord = interval.getInt(recordIndex);
            }

            interval.close();
        }

        return result.newRow()
                .add(intervalDays)
                .add(intervalSets)
                .add(intervalPullUps)
                .add(intervalRecord);
    }

}
