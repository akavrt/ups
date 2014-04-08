package com.akavrt.worko.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akavrt.worko.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class CompStatGroup extends RelativeLayout {
    @InjectView(R.id.section_title) TextView mTitleText;
    @InjectView(R.id.sets) TextView mSetsText;
    @InjectView(R.id.sets_delta) TextView mSetsDeltaText;
    @InjectView(R.id.total) TextView mPullUpsText;
    @InjectView(R.id.total_delta) TextView mPullUpsDeltaText;
    @InjectView(R.id.record) TextView mRecordText;
    @InjectView(R.id.record_delta) TextView mRecordDeltaText;

    public CompStatGroup(Context context) {
        super(context);
        init(null);
    }

    public CompStatGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CompStatGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    public void setSets(int value) {
        mSetsText.setText(Integer.toString(value));
    }

    public void setSetsDelta(int value) {
        mSetsDeltaText.setText(formatValue(value));
    }

    public void setPullUps(int value) {
        mPullUpsText.setText(Integer.toString(value));
    }

    public void setPullUpsDelta(int value) {
        mPullUpsDeltaText.setText(formatValue(value));
    }

    public void setRecord(int value) {
        mRecordText.setText(Integer.toString(value));
    }

    public void setRecordDelta(int value) {
        mRecordDeltaText.setText(formatValue(value));
    }
    
    private static String formatValue(int value) {
        return value != 0
                ? String.format("%+d", value)
                : Integer.toString(value);
    }
}
