package org.by1337.bauction.storage;

@FunctionalInterface
public interface EditSession<T> {
    void run(T value);
}
