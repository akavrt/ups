package com.akavrt.worko.utils;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class BusProvider {
    private static final Bus BUS = new Bus();
    private static final Bus WORKER_BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance() {
        return BUS;
    }

    public static Bus getWorkerInstance() {
        return WORKER_BUS;
    }
}
