package com.akavrt.worko.sensor;

import com.akavrt.worko.utils.DataLogger;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public interface LoggingSensorHelper extends SensorHelper {
    void setLogger(DataLogger logger);
}
