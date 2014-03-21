package com.akavrt.ups;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class StatsFragment extends Fragment implements OnScrollToTopListener {
    private ScrollView mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        mContainer = (ScrollView) rootView.findViewById(R.id.stats_scroll_group);

        return rootView;
    }

    @Override
    public void onScrollToTop() {
        mContainer.fullScroll(ScrollView.FOCUS_UP);
    }
}
