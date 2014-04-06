package com.akavrt.worko;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.akavrt.worko.events.ResetStatisticsEvent;
import com.akavrt.worko.utils.BusProvider;
import com.squareup.otto.Subscribe;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class StatsFragment extends Fragment implements OnScrollToTopListener {
    private ScrollView mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        getActivity().setTitle(R.string.stats);
        mContainer = (ScrollView) rootView.findViewById(R.id.stats_scroll_group);

        return rootView;
    }

    @Override
    public void onScrollToTop() {
        mContainer.fullScroll(ScrollView.FOCUS_UP);
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
    public void onShowClearStatDialog(ResetStatisticsEvent event) {
        // TODO add implementation
        Toast.makeText(getActivity(), R.string.clear_stat_toast, Toast.LENGTH_SHORT).show();
    }
}
