package com.akavrt.worko.service;

import com.akavrt.worko.events.PullUpWorkerEvent;
import com.akavrt.worko.utils.BusProvider;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class PullUpDetector {
    private static final float ALPHA_Z = 0.75f;
    private static final int POSITIVE = 0;
    private static final int NEGATIVE = 1;
    private static final int UNKNOWN = 2;
    private static final long PEAK_DURATION_THRESHOLD = 400L;
    private static final float PEAK_THRESHOLD = 0.4f;
    private float mPreviousValue;
    private long mPeakStart;
    private float mCurrentPeakType;
    private float mPreviousLargePeakType;
    private float mPeakExtremumValue;
    private boolean mIsFirstValue;

    public void process(float acceleration, long timeInMillis) {
//        float filtered = ALPHA_Z * mPreviousValue + (1 - ALPHA_Z) * acceleration;
        float filtered = acceleration;

        if (mIsFirstValue) {
            mIsFirstValue = false;

            mPeakStart = timeInMillis;
            mCurrentPeakType = filtered == 0
                    ? UNKNOWN
                    : (filtered > 0 ? POSITIVE : NEGATIVE);
            mPreviousLargePeakType = UNKNOWN;
            mPeakExtremumValue = filtered;
        } else {
            if (mCurrentPeakType == UNKNOWN && filtered != 0) {
                mCurrentPeakType = filtered > 0 ? POSITIVE : NEGATIVE;
            }

            if (Math.abs(mPreviousValue) > Math.abs(filtered)) {
                mPeakExtremumValue = mPreviousValue;
            }

            if (mPreviousValue * filtered < 0 || (mPreviousValue != 0 && filtered == 0)) {
                if (timeInMillis - mPeakStart > PEAK_DURATION_THRESHOLD
                        && Math.abs(mPeakExtremumValue) > PEAK_THRESHOLD) {
                    if (mCurrentPeakType == POSITIVE && mPreviousLargePeakType == NEGATIVE) {
                        // report this peak
                        BusProvider.getWorkerInstance().post(new PullUpWorkerEvent(timeInMillis));
                    }

                    // large peak detected
                    mPreviousLargePeakType = mCurrentPeakType;
                }

                // start watching new peak
                mPeakStart = timeInMillis;
                mCurrentPeakType = filtered == 0
                        ? UNKNOWN
                        : (filtered > 0 ? POSITIVE : NEGATIVE);
                mPeakExtremumValue = filtered;
            }
        }

        mPreviousValue = filtered;
    }
}
