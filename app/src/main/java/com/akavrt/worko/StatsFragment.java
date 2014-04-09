package com.akavrt.worko;

import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
public class StatsFragment extends Fragment implements OnScrollToTopListener,
        OnBackPressedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StatsFragment.class.getName();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        ButterKnife.inject(this, rootView);

        getActivity().setTitle(R.string.stats);

        register();

        return rootView;
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
    public void onScrollToTop() {
        mContainer.fullScroll(ScrollView.FOCUS_UP);
    }

    @Override
    public void hideContent() {
        mContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().initLoader(WEEK_STAT_LOADER_ID, null, this);
        getLoaderManager().initLoader(MONTH_STAT_LOADER_ID, null, this);
        getLoaderManager().initLoader(ALL_TIME_STAT_LOADER_ID, null, this);
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
                Log.d(TAG, "onDeleteComplete(), " + result + " records were deleted.");
                Toast.makeText(getActivity(), R.string.clear_stat_toast, Toast.LENGTH_SHORT).show();
            }
        };

        handler.startDelete(deleteToken, null, Sets.CONTENT_URI, null, null);
    }

    @Override
    public boolean onBackPressed() {
        return mAllTimeStatGroup.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Uri uri = null;
        switch (loaderId) {
            case WEEK_STAT_LOADER_ID:
                uri = WeekStat.CONTENT_URI;
                mWeekStatGroup.hideValues();
                break;

            case MONTH_STAT_LOADER_ID:
                uri = MonthStat.CONTENT_URI;
                mMonthStatGroup.hideValues();
                break;

            case ALL_TIME_STAT_LOADER_ID:
                uri = AllTimeStat.CONTENT_URI;
                mAllTimeStatGroup.hideValues();
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
                fillData(cursor, mWeekStatGroup);

                break;

            case MONTH_STAT_LOADER_ID:
                fillData(cursor, mMonthStatGroup);

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

                mAllTimeStatGroup.setDays(days);
                mAllTimeStatGroup.setSets(sets);
                mAllTimeStatGroup.setPullUps(pullUps);
                mAllTimeStatGroup.setRecord(record);
                mAllTimeStatGroup.showValues();

                break;
        }
    }

    private static void fillData(Cursor cursor, CompStatGroup group) {
        int currSets = 0;
        int currPullUps = 0;
        int currRecord = 0;
        int prevSets = 0;
        int prevPullUps = 0;
        int prevRecord = 0;
        if (cursor != null) {
            int setsIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.SETS);
            int pullUpsIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.PULL_UPS);
            int recordIndex = cursor.getColumnIndexOrThrow(StatisticsColumns.RECORD);

            if (cursor.moveToFirst()) {
                currSets = cursor.getInt(setsIndex);
                currPullUps = cursor.getInt(pullUpsIndex);
                currRecord = cursor.getInt(recordIndex);
            }

            if (cursor.moveToNext()) {
                prevSets = cursor.getInt(setsIndex);
                prevPullUps = cursor.getInt(pullUpsIndex);
                prevRecord = cursor.getInt(recordIndex);
            }
        }

        group.setSets(currSets);
        group.setSetsDelta(prevSets);
        group.setPullUps(currPullUps);
        group.setPullUpsDelta(prevPullUps);
        group.setRecord(currRecord);
        group.setRecordDelta(prevRecord);

        group.showValues();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
