package com.akavrt.worko.ui;

import android.content.Context;
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
public class TodayGroup extends RelativeLayout {
    @InjectView(R.id.today_sets) TextView mSetsText;

    public TodayGroup(Context context) {
        super(context);
        init();
    }

    public TodayGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TodayGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_today, this, true);

        ButterKnife.inject(this);
    }

    public TextView getSetsText() {
        return mSetsText;
    }

}
