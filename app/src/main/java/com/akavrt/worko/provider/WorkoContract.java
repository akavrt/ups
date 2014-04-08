package com.akavrt.worko.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public final class WorkoContract {
    public static final String CONTENT_AUTHORITY = "com.akavrt.worko.provider";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_SETS = "sets";
    static final String PATH_STAT = "stat";
    static final String PATH_WEEK = "week";
    static final String PATH_MONTH = "month";
    private static final String DIR = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY;
    private static final String ITEM = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY;

    interface SetsColumns {
        String DAY = "day";
        String PULL_UPS = "pull_ups";
    }

    public interface StatisticsColumns {
        String DAYS = "total_days";
        String SETS = "total_sets";
        String PULL_UPS = "total_pull_ups";
        String RECORD = "max_record";
    }

    public static final class Sets implements BaseColumns, SetsColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SETS).build();

        public static final String CONTENT_TYPE = DIR + ".set";
        public static final String CONTENT_ITEM_TYPE = ITEM + ".set";
    }

    public static final class AllTimeStat implements StatisticsColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_STAT).build();

        public static final String CONTENT_TYPE = DIR + ".stat";
    }

    public static final class WeekStat implements StatisticsColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_STAT).appendPath(PATH_WEEK).build();

        public static final String CONTENT_TYPE = DIR + ".stat.week";
    }

    public static final class MonthStat implements StatisticsColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_STAT).appendPath(PATH_MONTH).build();

        public static final String CONTENT_TYPE = DIR + ".stat.month";
    }

}
