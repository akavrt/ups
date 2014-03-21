package com.akavrt.ups.sensor;

import android.os.Handler;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public interface SensorHelper {
    void register(int rate, Handler handler);
    void unregister();
}
