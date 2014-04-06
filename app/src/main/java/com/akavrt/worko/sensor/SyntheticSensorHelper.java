package com.akavrt.worko.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Handler;

import com.akavrt.worko.utils.DataLogger;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class SyntheticSensorHelper extends BaseSensorHelper {
    private float[] mAccelerationValues;
    private float[] mRotationVector;
    private final float[] mRotationMatrix;
    private final float[] mInvertedRotationMatrix;
    private final float[] mRemappedAccelerationValues;

    public SyntheticSensorHelper(SensorManager manager) {
        super(manager);

        mRotationMatrix = new float[16];
        mInvertedRotationMatrix = new float[16];
        mRemappedAccelerationValues = new float[4];
    }

    @Override
    public void register(int rate, Handler handler) {
        super.register(rate, handler);

        Sensor accelerometer = mManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mManager.registerListener(this, accelerometer, rate, handler);

        Sensor magnetic = mManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mManager.registerListener(this, magnetic, rate, handler);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (mAccelerationValues == null) {
                    mAccelerationValues = new float[4];
                }

                System.arraycopy(event.values, 0, mAccelerationValues, 0, 3);

                if (mRotationVector != null) {
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, mRotationVector);
                    Matrix.invertM(mInvertedRotationMatrix, 0, mRotationMatrix, 0);
                    Matrix.multiplyMV(
                            mRemappedAccelerationValues, 0,
                            mInvertedRotationMatrix, 0,
                            mAccelerationValues, 0);

                    log(prepareLogMessage(mRemappedAccelerationValues));

                    mDetector.process(mRemappedAccelerationValues[2], event.timestamp / 1000000);
                }

                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                if (mRotationVector == null) {
                    mRotationVector = new float[3];
                }

                System.arraycopy(event.values, 0, mRotationVector, 0, 3);
                break;
        }
    }

    private static String prepareLogMessage(float[] values) {
        return String.format("%s\t%s\t%s",
                DataLogger.formatFloat(values[0]),
                DataLogger.formatFloat(values[1]),
                DataLogger.formatFloat(values[2]));
    }
}
