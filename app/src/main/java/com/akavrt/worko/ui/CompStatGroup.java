package com.akavrt.worko.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akavrt.worko.R;

import java.util.Map;

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

    public void setStatistics(Map<String, Integer> values) {
        mSetsText.setText(getFormattedValue(values, CompStatKeys.SETS, 0));
        mSetsDeltaText.setText(getFormattedValue(values, CompStatKeys.SETS_DELTA, 0));
        mPullUpsText.setText(getFormattedValue(values, CompStatKeys.PULL_UPS, 0));
        mPullUpsDeltaText.setText(getFormattedValue(values, CompStatKeys.PULL_UPS_DELTA, 0));
        mRecordText.setText(getFormattedValue(values, CompStatKeys.RECORD, 0));
        mRecordDeltaText.setText(getFormattedValue(values, CompStatKeys.RECORD_DELTA, 0));
    }

    private String getFormattedValue(Map<String, Integer> values, String key, int defValue) {
        int value = values.containsKey(key) ? values.get(key) : defValue;

        return String.format("%+d", value);
    }

    public interface CompStatKeys {
        String SETS = "sets";
        String SETS_DELTA = "sets_delta";
        String PULL_UPS = "pull_ups";
        String PULL_UPS_DELTA = "pull_ups_delta";
        String RECORD = "record";
        String RECORD_DELTA = "record_delta";
    }
}
