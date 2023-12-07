package org.by1337.bauction.util;

import org.by1337.bauction.serialize.SerializableToByteArray;

public interface UniqueName extends SerializableToByteArray {
    String getKey();

    int getSeed();

    long getPos();

    boolean canBeCompressToSeedAndPos();
}
