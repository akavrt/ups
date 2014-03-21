package com.akavrt.ups.sensor;

import com.akavrt.ups.utils.DataLogger;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public interface LoggingSensorHelper extends SensorHelper {
    void setLogger(DataLogger logger);
}
