package com.akavrt.ups.events;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class PullUpsAdjustEvent {
    public final int delta;

    public PullUpsAdjustEvent(int delta) {
        this.delta = delta;
    }
}
