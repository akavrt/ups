package com.akavrt.worko.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akavrt.worko.R;
import com.akavrt.worko.ServiceManager;
import com.akavrt.worko.service.CountingService;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class CounterGroup extends RelativeLayout {
    @InjectView(R.id.pull_ups_count) TextView mCountText;
    @InjectView(R.id.manage_counting) Button mStartStopButton;
    @InjectView(R.id.record_set) Button mRecordSetButton;
    @InjectView(R.id.manage_separator) View mDivider;
    @InjectView(R.id.prev_value) Button mPrevValueButton;
    @InjectView(R.id.next_value) Button mNextValueButton;
    private ServiceManager mManager;
    private int mCurrentCount;

    public CounterGroup(Context context) {
        super(context);
        init();
    }

    public CounterGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CounterGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void prepare() {
        mManager = new ServiceManager(getContext());

        if (mManager.isCounting()) {
            startCounting();
        } else {
            stopCounting();
        }
    }

    public void setValue(int count) {
        if (count * mCurrentCount == 0 && count + mCurrentCount > 0) {
            mRecordSetButton.setEnabled(count > 0);
        }

        mCurrentCount = count;
        mCountText.setText(Integer.toString(count));
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_counter, this, true);

        setupViews();
    }

    private void setupViews() {
        ButterKnife.inject(this);

        mStartStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                manageCounting();
            }
        });

        mRecordSetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mManager != null && mCurrentCount > 0) {
                    mManager.recordSet();
                }
            }
        });

        mPrevValueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mManager != null) {
                    mManager.decValue();
                }
            }
        });

        mNextValueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mManager != null) {
                    mManager.incValue();
                }
            }
        });
    }

    private void manageCounting() {
        if (CountingService.isRunning()) {
            stopCounting();
        } else {
            startCounting();
        }
    }

    private void startCounting() {
        if (mManager != null && !mManager.isCounting()) {
            mManager.startCounting();
        }

        mStartStopButton.setText(R.string.stop_counting);
        mStartStopButton.setGravity(Gravity.CENTER);

        mDivider.setVisibility(View.VISIBLE);
        mRecordSetButton.setVisibility(View.VISIBLE);

        mPrevValueButton.setVisibility(View.VISIBLE);
        mNextValueButton.setVisibility(View.VISIBLE);
    }

    private void stopCounting() {
        if (mManager != null && mManager.isCounting()) {
            mManager.stopCounting();
        }

        mStartStopButton.setText(R.string.start_counting);
        mStartStopButton.setGravity(Gravity.LEFT);

        mDivider.setVisibility(View.GONE);
        mRecordSetButton.setVisibility(View.GONE);

        mPrevValueButton.setVisibility(View.INVISIBLE);
        mNextValueButton.setVisibility(View.INVISIBLE);

        mCountText.setText("0");
    }
}
