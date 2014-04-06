package com.akavrt.worko.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import com.akavrt.worko.utils.DataLogger;
import com.akavrt.worko.utils.FileUtils;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class CompatSensorHelper implements SensorHelper {
    private static final String TAG = CompatSensorHelper.class.getName();

    private final BaseSensorHelper mInternalHelper;

    public CompatSensorHelper(Context context, boolean isLoggingEnabled) {
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        boolean isRotationVectorSensorAvailable =
                manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).size() > 0;
        boolean isLinearAccelerationSensorAvailable =
                manager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION).size() > 0;

        if (isRotationVectorSensorAvailable && isLinearAccelerationSensorAvailable) {
            Log.d(TAG, "Using synthetic sensors.");
            mInternalHelper = new SyntheticSensorHelper(manager);
        } else {
            Log.d(TAG, "Using raw sensors.");
            mInternalHelper = new RawSensorHelper(manager);
        }

        if (isLoggingEnabled && FileUtils.isExternalStorageWritable()) {
            DataLogger logger = new DataLogger(context);
            mInternalHelper.setLogger(logger);
        }
    }

    @Override
    public void register(int rate, Handler handler) {
        mInternalHelper.register(rate, handler);
    }

    @Override
    public void unregister() {
        mInternalHelper.unregister();
    }
}
