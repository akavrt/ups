package com.akavrt.worko.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.akavrt.worko.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class CompStatGroup extends TableLayout {
    @InjectView(R.id.section_title) TextView mTitleText;
    // current interval
    @InjectView(R.id.current_interval_title) TextView mCurrentIntervalText;
    @InjectView(R.id.curr_pull_ups) TextView mCurrentPullUpsText;
    @InjectView(R.id.curr_sets) TextView mCurrentSetsText;
    @InjectView(R.id.curr_record) TextView mCurrentRecordText;
    // previous interval
    @InjectView(R.id.previous_interval_title) TextView mPreviousIntervalText;
    @InjectView(R.id.prev_pull_ups) TextView mPreviousPullUpsText;
    @InjectView(R.id.prev_sets) TextView mPreviousSetsText;
    @InjectView(R.id.prev_record) TextView mPreviousRecordText;

    public CompStatGroup(Context context) {
        super(context);
        init(null);
    }

    public CompStatGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_comp_stat, this, true);

        ButterKnife.inject(this);

        if (attrs != null) {
            fillTitle(attrs);
        }
    }

    private void fillTitle(AttributeSet attrs) {
        TypedArray array = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.CompStatGroup, 0, 0);

        try {
            CharSequence title = array.getText(R.styleable.CompStatGroup_titleText);
            setTitle(title);
        } finally {
            array.recycle();
        }
    }

    public void setTitle(CharSequence title) {
        mTitleText.setText(title);
    }

    public void setTitle(int resId) {
        mTitleText.setText(resId);
    }

    public void setCurrentIntervalData(int pullUps, int sets, int record) {
        mCurrentPullUpsText.setText(Integer.toString(pullUps));
        mCurrentSetsText.setText(Integer.toString(sets));
        mCurrentRecordText.setText(Integer.toString(record));
    }

    public void setPreviousIntervalData(int pullUps, int sets, int record) {
        mPreviousPullUpsText.setText(Integer.toString(pullUps));
        mPreviousSetsText.setText(Integer.toString(sets));
        mPreviousRecordText.setText(Integer.toString(record));
    }

    public void showIntervals() {
        setIntervalsVisibility(View.VISIBLE);
    }

    public void hideIntervals() {
        setIntervalsVisibility(View.INVISIBLE);
    }

    private void setIntervalsVisibility(int visibility) {
        mCurrentIntervalText.setVisibility(visibility);
        mCurrentPullUpsText.setVisibility(visibility);
        mCurrentSetsText.setVisibility(visibility);
        mCurrentRecordText.setVisibility(visibility);

        mPreviousIntervalText.setVisibility(visibility);
        mPreviousPullUpsText.setVisibility(visibility);
        mPreviousSetsText.setVisibility(visibility);
        mPreviousRecordText.setVisibility(visibility);
    }

    public void setIntervalTitles(
            CharSequence currentIntervalTitle,
            CharSequence previousIntervalTitle) {
        mCurrentIntervalText.setText(currentIntervalTitle);
        mPreviousIntervalText.setText(previousIntervalTitle);
    }
}
