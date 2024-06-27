package org.by1337.bauction.util.time;

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
        if (x < 50) return "&a" + x;
        else if (x < 100) return "&e" + x;
        else if (x < 150) return "&4" + x;
        else return "&c" + x;
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
