package com.akavrt.ups.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.akavrt.ups.service.PullUpDetector;
import com.akavrt.ups.utils.DataLogger;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public abstract class BaseSensorHelper implements LoggingSensorHelper, SensorEventListener {
    protected final SensorManager mManager;
    protected PullUpDetector mDetector;
    private DataLogger mLogger;

    public BaseSensorHelper(SensorManager manager) {
        this.mManager = manager;

        mDetector = new PullUpDetector();
    }

    public void setLogger(DataLogger logger) {
        this.mLogger = logger;
    }

    protected void log(String message) {
        if (mLogger != null) {
            mLogger.log(message);
        }
    }

    @Override
    public void register(int rate, Handler handler) {
        if (mLogger != null) {
            mLogger.start();
        }
    }

    @Override
    public void unregister() {
        if (mLogger != null) {
            mLogger.stop();
        }

        mManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // do nothing
    }
}
