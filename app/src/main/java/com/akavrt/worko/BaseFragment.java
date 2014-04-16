package com.akavrt.worko;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public abstract class BaseFragment extends Fragment{
    private static final String IS_MENU_ITEMS_VISIBLE = "is_menu_items_visible";
    protected boolean mIsMenuItemVisible;

    protected abstract ViewGroup getContainerView();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mIsMenuItemVisible = savedInstanceState == null ||
                savedInstanceState.getBoolean(IS_MENU_ITEMS_VISIBLE, true);

        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_MENU_ITEMS_VISIBLE, mIsMenuItemVisible);
    }

    public void hideContent() {
        getContainerView().setVisibility(View.INVISIBLE);
    }

    public void hideMenuItems() {
        mIsMenuItemVisible = false;
        ActivityCompat.invalidateOptionsMenu(getActivity());
    }

    public void showMenuItems() {
        mIsMenuItemVisible = true;
        ActivityCompat.invalidateOptionsMenu(getActivity());
    }

    public void onScrollToTop() {
        if (getContainerView() instanceof ScrollView) {
            ((ScrollView) getContainerView()).fullScroll(ScrollView.FOCUS_UP);
        }
    }
}
