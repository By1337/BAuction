package org.by1337.bauction.db;

import java.util.HashMap;
import java.util.function.Supplier;

public class StorageMap<K, V> extends HashMap<K, V> {
    public <X extends Throwable> V getOrThrow(K key, Supplier<? extends X> def) throws X {
        if (!containsKey(key)) {
            throw def.get();
        }
        return get(key);
    }
}
