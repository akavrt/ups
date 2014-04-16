package com.akavrt.worko;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.akavrt.worko.events.ResetStatisticsEvent;
import com.akavrt.worko.ui.AllTimeStatGroup;
import com.akavrt.worko.ui.CompStatGroup;
import com.akavrt.worko.utils.BusProvider;
import com.squareup.otto.Subscribe;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.akavrt.worko.provider.WorkoContract.AllTimeStat;
import static com.akavrt.worko.provider.WorkoContract.MonthStat;
import static com.akavrt.worko.provider.WorkoContract.Sets;
import static com.akavrt.worko.provider.WorkoContract.StatisticsColumns;
import static com.akavrt.worko.provider.WorkoContract.WeekStat;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class StatisticsFragment extends BaseFragment implements OnBackPressedListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StatisticsFragment.class.getName();
    private static final String PREFS_NAME = "StatisticsPreferencesFile";
    private static final String PREFS_LAST_RESET_DATE = "LastResetDate";
    private static final int WEEK_STAT_LOADER_ID = 1;
    private static final int MONTH_STAT_LOADER_ID = 2;
    private static final int ALL_TIME_STAT_LOADER_ID = 3;
    private static final String[] sProjection = {
            StatisticsColumns.DAYS,
            StatisticsColumns.SETS,
            StatisticsColumns.PULL_UPS,
            StatisticsColumns.RECORD
    };
    // views
    @InjectView(R.id.stats_scroll_group) ScrollView mContainer;
    @InjectView(R.id.week_stat) CompStatGroup mWeekStatGroup;
    @InjectView(R.id.month_stat) CompStatGroup mMonthStatGroup;
    @InjectView(R.id.all_time_stat) AllTimeStatGroup mAllTimeStatGroup;
    private SimpleDateFormat mMonthFormat;
    private SharedPreferences mPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);
        ButterKnife.inject(this, rootView);

        prepareDateFormats();
        mPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mWeekStatGroup.setIntervalTitles(
                getString(R.string.this_week),
                getString(R.string.last_week));

        getActivity().setTitle(R.string.stats);

        register();

        return rootView;
    }

    @Override
    protected ViewGroup getContainerView() {
        return mContainer;
    }

    private void prepareDateFormats() {
        String[] monthNames = getResources().getStringArray(R.array.month_names);
        if (monthNames.length > 0) {
            DateFormatSymbols symbols = new DateFormatSymbols();
            symbols.setMonths(monthNames);
            mMonthFormat = new SimpleDateFormat("MMMM", symbols);
        } else {
            mMonthFormat = new SimpleDateFormat("MMMM");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unregister();
    }

    private void register() {
        try {
            ((MainActivity) getActivity()).registerBackListener(this);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Wrong hosting activity", ex);
        }
    }

    private void unregister() {
        try {
            ((MainActivity) getActivity()).unregisterBackListener(this);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Wrong hosting activity", ex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().initLoader(WEEK_STAT_LOADER_ID, null, this);

        long timeInMillis = mPreferences.getLong(PREFS_LAST_RESET_DATE, -1);
        setResetDate(timeInMillis);
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onStatisticsReset(ResetStatisticsEvent event) {
        clearStat();
    }

    private void clearStat() {
        int deleteToken = 1;
        AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {

            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                Log.d(TAG, "onDeleteComplete(), " + result + " records werOe deleted.");
                Toast.makeText(getActivity(), R.string.clear_stat_toast, Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(PREFS_LAST_RESET_DATE, new Date().getTime());
                editor.apply();

                setResetDate(new Date().getTime());
            }
        };

        handler.startDelete(deleteToken, null, Sets.CONTENT_URI, null, null);
    }

    @Override
    public boolean onBackPressed() {
        return mAllTimeStatGroup.onBackPressed();
    }

    private void setResetDate(long timeInMillis) {
        if (timeInMillis < 0) {
            timeInMillis = new Date().getTime();
        }

        Locale currentLocale = getResources().getConfiguration().locale;
        Calendar currentDate = Calendar.getInstance(currentLocale);

        Calendar resetDate = Calendar.getInstance(currentLocale);
        resetDate.setTimeInMillis(timeInMillis);

        String formattedDate;
        if (currentDate.get(Calendar.YEAR) == resetDate.get(Calendar.YEAR)) {
            SimpleDateFormat format = new SimpleDateFormat("d MMMM");
            formattedDate = format.format(resetDate.getTime());
        } else {
            SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");
            formattedDate = format.format(resetDate.getTime());
        }

        String formatted = String.format("%s %s",
                getString(R.string.last_reset),
                formattedDate);

        mAllTimeStatGroup.setLastResetDate(formatted);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Uri uri = null;
        switch (loaderId) {
            case WEEK_STAT_LOADER_ID:
                uri = WeekStat.CONTENT_URI;
//                mWeekStatGroup.hideIntervals();
                mWeekStatGroup.setVisibility(View.INVISIBLE);
                break;

            case MONTH_STAT_LOADER_ID:
                uri = MonthStat.CONTENT_URI;
//                mMonthStatGroup.hideIntervals();
                mMonthStatGroup.setVisibility(View.INVISIBLE);
                break;

            case ALL_TIME_STAT_LOADER_ID:
                uri = AllTimeStat.CONTENT_URI;
//                mAllTimeStatGroup.hideData();
                mAllTimeStatGroup.setVisibility(View.INVISIBLE);
                break;
        }

        return uri == null
                ? null
                : new CursorLoader(getActivity(), uri, sProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case WEEK_STAT_LOADER_ID:
                fillWeekData(cursor);
                getLoaderManager().initLoader(MONTH_STAT_LOADER_ID, null, this);

                break;

            case MONTH_STAT_LOADER_ID:
                fillMonthData(cursor);
                getLoaderManager().initLoader(ALL_TIME_STAT_LOADER_ID, null, this);

                break;

            case ALL_TIME_STAT_LOADER_ID:
                int days = 0;
                int sets = 0;
                int pullUps = 0;
                int record = 0;
                if (cursor != null) {
                    int daysIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.DAYS);
                    int setsIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.SETS);
                    int pullUpsIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.PULL_UPS);
                    int recordIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.RECORD);

                    if (cursor.moveToFirst()) {
                        days = cursor.getInt(daysIndex);
                        sets = cursor.getInt(setsIndex);
                        pullUps = cursor.getInt(pullUpsIndex);
                        record = cursor.getInt(recordIndex);
                    }
                }

                mAllTimeStatGroup.setData(days, pullUps, sets, record);
//                mAllTimeStatGroup.showData();
                mAllTimeStatGroup.setVisibility(View.VISIBLE);

                break;
        }
    }

    private void fillWeekData(Cursor cursor) {
        fillData(cursor, mWeekStatGroup, false);
    }

    private void fillMonthData(Cursor cursor) {
        fillData(cursor, mMonthStatGroup, true);
    }

    private void fillData(Cursor cursor, CompStatGroup group, boolean isMonthData) {
        String currIntervalName = null;
        int currPullUps = 0;
        int currSets = 0;
        int currRecord = 0;

        String prevIntervalName = null;
        int prevPullUps = 0;
        int prevSets = 0;
        int prevRecord = 0;

        if (cursor != null) {
            int intervalIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.INTERVAL_START);
            int pullUpsIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.PULL_UPS);
            int setsIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.SETS);
            int recordIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.RECORD);

            if (cursor.moveToFirst()) {
                currSets = cursor.getInt(setsIndex);
                currPullUps = cursor.getInt(pullUpsIndex);
                currRecord = cursor.getInt(recordIndex);

                if (isMonthData) {
                    long currIntervalStart = cursor.getLong(intervalIndex);
                    Date date = new Date(currIntervalStart);
                    currIntervalName = mMonthFormat.format(date);
                }
            }

            if (cursor.moveToNext()) {
                prevSets = cursor.getInt(setsIndex);
                prevPullUps = cursor.getInt(pullUpsIndex);
                prevRecord = cursor.getInt(recordIndex);

                if (isMonthData) {
                    long prevIntervalStart = cursor.getLong(intervalIndex);
                    Date date = new Date(prevIntervalStart);
                    prevIntervalName = mMonthFormat.format(date);
                }
            }
        }

        group.setCurrentIntervalData(currPullUps, currSets, currRecord);
        group.setPreviousIntervalData(prevPullUps, prevSets, prevRecord);

        if (!TextUtils.isEmpty(currIntervalName) && !TextUtils.isEmpty(prevIntervalName)) {
            group.setIntervalTitles(currIntervalName, prevIntervalName);
        }

//        group.showIntervals();
        group.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
