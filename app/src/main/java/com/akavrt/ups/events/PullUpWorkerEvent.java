package com.akavrt.ups.events;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class PullUpWorkerEvent {
    public final long timestamp;

    public PullUpWorkerEvent(long timestamp) {
        this.timestamp = timestamp;
    }
}
