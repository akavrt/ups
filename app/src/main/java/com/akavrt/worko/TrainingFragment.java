package com.akavrt.worko;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class TrainingFragment extends Fragment implements OnScrollToTopListener {
    private static final String TAG = TrainingFragment.class.getName();

    private ServiceManager mManager;
    @InjectView(R.id.pull_ups_count) TextView countText;
    @InjectView(R.id.manage_counting) Button startStopButton;
    @InjectView(R.id.prev_value) Button prevValueButton;
    @InjectView(R.id.next_value) Button nextValueButton;
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
            startStopButton.setText(R.string.stop_counting);

            prevValueButton.setVisibility(View.VISIBLE);
            nextValueButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
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

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageCounting();
            }
        });

        prevValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decValue();
            }
        });

        nextValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incValue();
            }
        });
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

        startStopButton.setText(R.string.stop_counting);

        prevValueButton.setVisibility(View.VISIBLE);
        nextValueButton.setVisibility(View.VISIBLE);
    }

    private void stopCounting() {
        mManager.stopCounting();

        startStopButton.setText(R.string.start_counting);

        prevValueButton.setVisibility(View.INVISIBLE);
        nextValueButton.setVisibility(View.INVISIBLE);

        setValue(0);
    }

    private void decValue() {
        BusProvider.getInstance().post(new PullUpsAdjustEvent(-1));
    }

    private void incValue() {
        BusProvider.getInstance().post(new PullUpsAdjustEvent(1));
    }

    private void setValue(int value) {
        countText.setText(Integer.toString(value));
    }
}