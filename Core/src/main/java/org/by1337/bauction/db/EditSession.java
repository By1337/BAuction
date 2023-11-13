package org.by1337.bauction.db;

@FunctionalInterface
public interface EditSession<T> {
    void run(T value);
}
