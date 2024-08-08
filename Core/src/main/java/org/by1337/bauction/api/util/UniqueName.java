package org.by1337.bauction.api.util;

import org.by1337.bauction.api.serialize.SerializableToByteArray;

@Deprecated(forRemoval = true)
public interface UniqueName extends SerializableToByteArray {
    String getKey();

    int getSeed();

    long getPos();

    boolean canBeCompressToSeedAndPos();
}