package com.akavrt.worko;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.akavrt.worko.service.CountingService;

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
}
