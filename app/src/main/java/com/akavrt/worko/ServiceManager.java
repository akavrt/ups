package com.akavrt.worko;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.akavrt.worko.events.PullUpsAdjustEvent;
import com.akavrt.worko.events.RecordSetEvent;
import com.akavrt.worko.service.CountingService;
import com.akavrt.worko.utils.BusProvider;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class ServiceManager {
    private static final String TAG = ServiceManager.class.getName();

    private final Context mContext;

    public ServiceManager(Context context) {
        this.mContext = context;
    }

    public void startCounting() {
        Log.d(TAG, "Starting the service.");

        Intent service = new Intent(mContext, CountingService.class);
        mContext.startService(service);
    }

    public void stopCounting() {
        Log.d(TAG, "Stopping the service.");

        Intent service = new Intent(mContext, CountingService.class);
        mContext.stopService(service);
    }

    public boolean isCounting() {
        return CountingService.isRunning();
    }

    public void decValue() {
        BusProvider.getInstance().post(new PullUpsAdjustEvent(-1));
    }

    public void incValue() {
        BusProvider.getInstance().post(new PullUpsAdjustEvent(1));
    }

    public void recordSet() {
        BusProvider.getInstance().post(new RecordSetEvent());
    }
}
