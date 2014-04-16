package com.akavrt.worko;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.akavrt.worko.events.PullUpEvent;
import com.akavrt.worko.service.CountingService;
import com.akavrt.worko.ui.CounterGroup;
import com.akavrt.worko.ui.TodayGroup;
import com.akavrt.worko.utils.BusProvider;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.akavrt.worko.provider.WorkoContract.Sets;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class TrainingFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = TrainingFragment.class.getName();
    private static final String[] sProjection = {
            Sets.PULL_UPS
    };
    @InjectView(R.id.today_scroll_group) ScrollView mContainer;
    @InjectView(R.id.counter_group) CounterGroup mCounterGroup;
    @InjectView(R.id.today_group) TodayGroup mTodayGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_training, container, false);

        getActivity().setTitle(R.string.training);
        setHasOptionsMenu(true);

        ButterKnife.inject(this, rootView);

        return rootView;
    }

    @Override
    protected ViewGroup getContainerView() {
        return mContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCounterGroup.prepare();
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

        mCounterGroup.setValue(event.count);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        String todaySt = Long.toString(CountingService.getTodayInMillis(getActivity()) / 1000);
        mTodayGroup.getSetsText().setVisibility(View.INVISIBLE);

        return new CursorLoader(
                getActivity(),
                Sets.CONTENT_URI,
                sProjection,
                Sets.DAY + " = ?",
                new String[]{todaySt},
                Sets._ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            int pullUpsIndex = cursor.getColumnIndexOrThrow(Sets.PULL_UPS);

            StringBuilder sb = new StringBuilder();
            int sets = 0;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int pullUps = cursor.getInt(pullUpsIndex);
                sb.append(pullUps);

                sets++;

                if (!cursor.isLast()) {
                    sb.append(sets % 8 == 0 ? "\n" : " \u2014 ");
                }
            }

            mTodayGroup.getSetsText().setText(sb.toString());
        } else {
            mTodayGroup.getSetsText().setText(R.string.no_phys_activity);
        }

        mTodayGroup.getSetsText().setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}