package org.by1337.bauction.util.id;

public class UniqueIdGenerator {
    private long pos;

    public UniqueIdGenerator(long pos) {
        this.pos = pos;
    }

    public UniqueIdGenerator() {
    }

    public synchronized long nextId(){
        return pos++;
    }
    public synchronized void setPos(int pos) {
        this.pos = pos;
    }

    public synchronized long getPos() {
        return pos;
    }
}
