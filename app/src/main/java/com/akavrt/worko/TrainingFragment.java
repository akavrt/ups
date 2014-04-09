package com.akavrt.worko;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.akavrt.worko.events.PullUpEvent;
import com.akavrt.worko.events.PullUpsAdjustEvent;
import com.akavrt.worko.service.CountingService;
import com.akavrt.worko.utils.BusProvider;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.akavrt.worko.provider.WorkoContract.Sets;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class TrainingFragment extends Fragment implements OnScrollToTopListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = TrainingFragment.class.getName();
    private static final String[] sProjection = {
            Sets.PULL_UPS
    };

    private ServiceManager mManager;
    @InjectView(R.id.pull_ups_count) TextView mCountText;
    @InjectView(R.id.manage_counting) Button mStartStopButton;
    @InjectView(R.id.prev_value) Button mPrevValueButton;
    @InjectView(R.id.next_value) Button mNextValueButton;
    @InjectView(R.id.today_sets) TextView mTodaySetsText;
    @InjectView(R.id.today_scroll_group) ScrollView mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_today, container, false);

        getActivity().setTitle(R.string.training);
        setupViews(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new ServiceManager(getActivity());

        Log.d(TAG, "onActivityCreated(), mManager.isCounting() = " + mManager.isCounting());

        if (mManager.isCounting()) {
            mStartStopButton.setText(R.string.stop_counting);

            mPrevValueButton.setVisibility(View.VISIBLE);
            mNextValueButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onPullUpDetected(PullUpEvent event) {
        Log.d(TAG, "onPullUpDetected(), count = " + event.count);

        setValue(event.count);
    }

    private void setupViews(View rootView) {
        ButterKnife.inject(this, rootView);

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageCounting();
            }
        });

        mPrevValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decValue();
            }
        });

        mNextValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incValue();
            }
        });
    }

    @Override
    public void hideContent() {
        mContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onScrollToTop() {
        mContainer.fullScroll(ScrollView.FOCUS_UP);
    }

    private void manageCounting() {
        if (CountingService.isRunning()) {
            stopCounting();
        } else {
            startCounting();
        }
    }

    private void startCounting() {
        mManager.startCounting();

        mStartStopButton.setText(R.string.stop_counting);

        mPrevValueButton.setVisibility(View.VISIBLE);
        mNextValueButton.setVisibility(View.VISIBLE);
    }

    private void stopCounting() {
        mManager.stopCounting();

        mStartStopButton.setText(R.string.start_counting);

        mPrevValueButton.setVisibility(View.INVISIBLE);
        mNextValueButton.setVisibility(View.INVISIBLE);

        setValue(0);
    }

    private void decValue() {
        BusProvider.getInstance().post(new PullUpsAdjustEvent(-1));
    }

    private void incValue() {
        BusProvider.getInstance().post(new PullUpsAdjustEvent(1));
    }

    private void setValue(int value) {
        mCountText.setText(Integer.toString(value));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        String todaySt = Long.toString(CountingService.getTodayInMillis(getActivity()) / 1000);
        mTodaySetsText.setVisibility(View.INVISIBLE);

        return new CursorLoader(
                getActivity(),
                Sets.CONTENT_URI,
                sProjection,
                Sets.DAY + " = ?",
                new String[] {todaySt},
                Sets._ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            int pullUpsIndex = cursor.getColumnIndexOrThrow(Sets.PULL_UPS);

            StringBuilder sb = new StringBuilder();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int pullUps = cursor.getInt(pullUpsIndex);
                sb.append(pullUps);

                if (!cursor.isLast()) {
                    sb.append(" \u2014 ");
                }
            }

            mTodaySetsText.setText(sb.toString());
        } else {
            mTodaySetsText.setText(R.string.no_phys_activity);
        }

        mTodaySetsText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}