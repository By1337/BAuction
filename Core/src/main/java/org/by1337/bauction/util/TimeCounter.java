package org.by1337.bauction.util;

public class TimeCounter {
    private final long time;

    public TimeCounter() {
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - time;
    }
}
