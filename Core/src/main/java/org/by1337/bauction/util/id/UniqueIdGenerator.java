package org.by1337.bauction.util.id;

public class UniqueIdGenerator {
    private int pos;

    public UniqueIdGenerator(int pos) {
        this.pos = pos;
    }

    public UniqueIdGenerator() {
    }

    public synchronized int nextId(){
        return pos++;
    }
    public synchronized void setPos(int pos) {
        this.pos = pos;
    }

    public synchronized int getPos() {
        return pos;
    }
}
