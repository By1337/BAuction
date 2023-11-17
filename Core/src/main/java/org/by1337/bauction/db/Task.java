package org.by1337.bauction.db;

import org.by1337.bauction.db.StorageException;

@FunctionalInterface
public interface Task<T> {
    T run() throws StorageException;
}
