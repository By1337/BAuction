package org.by1337.bauction.util;

public class TimeCounter {
    private long time;
    private final long timeStart;

    public TimeCounter() {
        this.time = System.currentTimeMillis();
        this.timeStart = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - time;
    }

    public String getTimeColored() {
        return getColored(getTime());
    }

    private String getColored(long x) {
        if (x < 50) return "&#01FF00" + x;
        else if (x < 100) return "&#E7FF00" + x;
        else if (x < 150) return "&#FF9B00" + x;
        else return "&#FF0000" + x;
    }

    public long getTotalTime() {
        return System.currentTimeMillis() - timeStart;
    }

    public String getTotalTimeColored() {
        return getColored(getTotalTime());
    }

    public void reset() {
        this.time = System.currentTimeMillis();
    }

}
