package com.akavrt.ups.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Handler;

import com.akavrt.ups.utils.DataLogger;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class RawSensorHelper extends BaseSensorHelper {
    private static final float ALPHA = 0.8f;
    private float[] mMagneticValues;
    private float[] mGravityValues;
    private final float[] mLinearAccelerationValues;
    private final float[] mRotationMatrix;
    private final float[] mInvertedRotationMatrix;
    private final float[] mRemappedAccelerationValues;

    public RawSensorHelper(SensorManager manager) {
        super(manager);

        mRotationMatrix = new float[16];
        mInvertedRotationMatrix = new float[16];
        mRemappedAccelerationValues = new float[4];
        mLinearAccelerationValues = new float[4];
    }

    @Override
    public void register(int rate, Handler handler) {
        super.register(rate, handler);

        Sensor accelerometer = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mManager.registerListener(this, accelerometer, rate, handler);

        Sensor magnetic = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mManager.registerListener(this, magnetic, rate, handler);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (mGravityValues == null) {
                    mGravityValues = new float[4];
                    System.arraycopy(event.values, 0, mGravityValues, 0, 3);
                    System.arraycopy(event.values, 0, mLinearAccelerationValues, 0, 3);
                } else {
                    // applying low-pass filter to extract gravity
                    mGravityValues[0] = ALPHA * mGravityValues[0] + (1 - ALPHA) * event.values[0];
                    mGravityValues[1] = ALPHA * mGravityValues[1] + (1 - ALPHA) * event.values[1];
                    mGravityValues[2] = ALPHA * mGravityValues[2] + (1 - ALPHA) * event.values[2];

                    // applying high-pass filter to extract linear acceleration
                    mLinearAccelerationValues[0] = event.values[0] - mGravityValues[0];
                    mLinearAccelerationValues[1] = event.values[1] - mGravityValues[1];
                    mLinearAccelerationValues[2] = event.values[2] - mGravityValues[2];
                }

                if (mMagneticValues != null &&
                        SensorManager.getRotationMatrix(
                                mRotationMatrix,
                                null,
                                mGravityValues,
                                mMagneticValues)) {
                    Matrix.invertM(mInvertedRotationMatrix, 0, mRotationMatrix, 0);
                    Matrix.multiplyMV(
                            mRemappedAccelerationValues, 0,
                            mInvertedRotationMatrix, 0,
                            mLinearAccelerationValues, 0);

                    mFiltered[0] = filter(mFiltered[0], mRemappedAccelerationValues[2], ALPHA_Z1);
                    mFiltered[1] = filter(mFiltered[1], mRemappedAccelerationValues[2], ALPHA_Z2);
                    mFiltered[2] = filter(mFiltered[2], mRemappedAccelerationValues[2], ALPHA_Z3);

                    log(prepareLogMessage(mRemappedAccelerationValues, mFiltered));

                    mDetector.process(mRemappedAccelerationValues[2], event.timestamp / 1000000);
                }

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                if (mMagneticValues == null) {
                    mMagneticValues = new float[3];
                }

                System.arraycopy(event.values, 0, mMagneticValues, 0, 3);
                break;
        }
    }

    private static final float ALPHA_Z1 = 0.70f;
    private static final float ALPHA_Z2 = 0.75f;
    private static final float ALPHA_Z3 = 0.78f;
    private float[] mFiltered = new float[3];

    private static float filter(float oldValue, float newValue, float alpha) {
        return alpha * oldValue + (1 - alpha) * newValue;
    }

    private static String prepareLogMessage(float[] values, float[] filtered) {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s",
                DataLogger.formatFloat(values[0]),
                DataLogger.formatFloat(values[1]),
                DataLogger.formatFloat(values[2]),
                DataLogger.formatFloat(filtered[0]),
                DataLogger.formatFloat(filtered[1]),
                DataLogger.formatFloat(filtered[2]));
    }
}
