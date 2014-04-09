package com.akavrt.worko;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.akavrt.worko.service.Notificator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class MainActivity extends ActionBarActivity {
    private static final String CURRENT_ITEM = "current_item";
    private interface FragmentTags {
        String TRAINING = "training";
        String STATISTICS = "statistics";
    }
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mFragmentTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private int mCurrentItem = -1;
    private List<OnBackPressedListener> mBackListeners;
    private boolean mIsReplaceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = getTitle();
        mDrawerTitle = getString(R.string.app_name);

        mBackListeners = new ArrayList<OnBackPressedListener>();

        setupViews();

        if (savedInstanceState == null) {
            selectItem(0);
        } else {
            mCurrentItem = savedInstanceState.getInt(CURRENT_ITEM, -1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(CURRENT_ITEM, mCurrentItem);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (isStatusBarNotification(intent)) {
            selectItem(0);
        }
    }

    private boolean isStatusBarNotification(Intent intent) {
        return intent != null && intent.getBooleanExtra(Notificator.STATUS_BAR_NOTIFICATION, false);
    }


    private void setupViews() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mFragmentTitles = new String[]{
                getString(R.string.training),
                getString(R.string.stats)
        };

        ListAdapter adapter = new ArrayAdapter<String>(
                this, R.layout.drawer_list_item, mFragmentTitles);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);

                if (mIsReplaceFragment) {
                    mIsReplaceFragment = false;

                    Fragment fragment;
                    String tag;
                    if (mCurrentItem == 0) {
                        fragment = new TrainingFragment();
                        tag = FragmentTags.TRAINING;
                    } else {
                        fragment = new StatsFragment();
                        tag = FragmentTags.STATISTICS;
                    }

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment, tag)
                            .commit();
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position != mCurrentItem) {
                int prevItem = mCurrentItem;

                mCurrentItem = position;
                mIsReplaceFragment = true;

                // Highlight the selected item, update the title, and close the drawer
                mDrawerList.setItemChecked(mCurrentItem, true);
                setTitle(mFragmentTitles[mCurrentItem]);

                String tag = prevItem == 0
                        ? FragmentTags.TRAINING
                        : FragmentTags.STATISTICS;

                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                ((OnScrollToTopListener) fragment).hideContent();
            }

            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void selectItem(int position) {
        if (position != mCurrentItem) {
            mCurrentItem = position;

            Fragment fragment;
            String tag;
            if (position == 0) {
                fragment = new TrainingFragment();
                tag = FragmentTags.TRAINING;
            } else {
                fragment = new StatsFragment();
                tag = FragmentTags.STATISTICS;
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment, tag)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mFragmentTitles[position]);
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        for (OnBackPressedListener listener : mBackListeners) {
            if (listener.onBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }

    public void registerBackListener(OnBackPressedListener listener) {
        mBackListeners.add(listener);
    }

    public void unregisterBackListener(OnBackPressedListener listener) {
        mBackListeners.remove(listener);
    }
}
