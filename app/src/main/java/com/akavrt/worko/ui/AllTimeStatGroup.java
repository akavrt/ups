package com.akavrt.worko.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.akavrt.worko.R;
import com.akavrt.worko.events.ResetStatisticsEvent;
import com.akavrt.worko.utils.BusProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class AllTimeStatGroup extends FrameLayout {
    private static final String TAG = AllTimeStatGroup.class.getName();

    private static final String SUPER_STATE = "super_state";
    private static final String IS_IN_DIALOG_MODE = "is_in_dialog_mode";

    @InjectView(R.id.reset_toggle) ImageButton mResetToggleButton;
    @InjectView(R.id.card_content) ViewGroup mCardContent;
    @InjectView(R.id.all_time_days) TextView mDaysText;
    @InjectView(R.id.all_time_sets) TextView mSetsText;
    @InjectView(R.id.all_time_total) TextView mPullUpsText;
    @InjectView(R.id.all_time_record) TextView mRecordText;
    private View mCardDialog;
    private Button mDialogPositiveButton;
    private Button mDialogNegativeButton;
    private int mShortAnimationDuration;
    private boolean mIsInDialogMode;
    private boolean mIsDialogInflated;
    private Drawable mStaticDotsDrawable;
    private Drawable mDynamicDotsDrawable;

    public AllTimeStatGroup(Context context) {
        super(context);
        init();
    }

    public AllTimeStatGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AllTimeStatGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_all_time_stat, this, true);

        ButterKnife.inject(this);

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mStaticDotsDrawable = getResources().getDrawable(R.drawable.ic_training_dots_selected);
        mDynamicDotsDrawable = getResources().getDrawable(R.drawable.dots_button);

        setupViews();
    }

    private void setupViews() {
        mResetToggleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mIsInDialogMode) {
                    showContent();
                } else {
                    showDialog();
                }

                mIsInDialogMode = !mIsInDialogMode;
            }
        });
    }

    private void showContent() {
        crossfade(mCardContent, mCardDialog);
        mResetToggleButton.setImageDrawable(mDynamicDotsDrawable);
    }

    private void showDialog() {
        if (!mIsDialogInflated) {
            setupDialog();
        }

        crossfade(mCardDialog, mCardContent);
        mResetToggleButton.setImageDrawable(mStaticDotsDrawable);
    }

    private void setupDialog() {
        mIsDialogInflated = true;

        mCardDialog = ((ViewStub) findViewById(R.id.stub_card_dialog)).inflate();
        mDialogPositiveButton = (Button) mCardDialog.findViewById(R.id.clear_stats_positive);
        mDialogNegativeButton = (Button) mCardDialog.findViewById(R.id.clear_stats_negative);

        mDialogPositiveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                showContent(true);
            }
        });

        mDialogNegativeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                showContent(false);
            }
        });
    }

    private void showContent(boolean resetData) {
        crossfade(mCardContent, mCardDialog);
        mResetToggleButton.setImageDrawable(mDynamicDotsDrawable);
        mIsInDialogMode = false;

        if (resetData) {
            BusProvider.getInstance().post(new ResetStatisticsEvent());
        }
    }

    private void crossfade(final View viewToAppear, final View viewToFade) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            crossfadePreHoneycomb(viewToAppear, viewToFade);
        } else {
            crossfadeHoneycomb(viewToAppear, viewToFade);
        }
    }

    private void crossfadePreHoneycomb(final View viewToAppear, final View viewToFade) {
        viewToFade.setVisibility(View.INVISIBLE);
        viewToAppear.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void crossfadeHoneycomb(final View viewToAppear, final View viewToFade) {
        viewToAppear.setAlpha(0f);
        viewToAppear.setVisibility(View.VISIBLE);

        viewToAppear.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        viewToFade.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewToFade.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        bundle.putBoolean(IS_IN_DIALOG_MODE, mIsInDialogMode);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsInDialogMode = bundle.getBoolean(IS_IN_DIALOG_MODE, false);

            super.onRestoreInstanceState(bundle.getParcelable(SUPER_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }

        if (mIsInDialogMode) {
            showDialog();
        }
    }

    public boolean onBackPressed() {
        boolean needToHandle = mIsInDialogMode;
        if (needToHandle) {
            showContent();
            mIsInDialogMode = !mIsInDialogMode;
        }

        return needToHandle;
    }

    public void setDays(int value) {
        mDaysText.setText(Integer.toString(value));
    }

    public void setSets(int value) {
        mSetsText.setText(Integer.toString(value));
    }

    public void setPullUps(int value) {
        mPullUpsText.setText(Integer.toString(value));
    }

    public void setRecord(int value) {
        mRecordText.setText(Integer.toString(value));
    }
}
