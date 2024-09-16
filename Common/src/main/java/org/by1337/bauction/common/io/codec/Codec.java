package org.by1337.bauction.common.io.codec;

import org.by1337.btcp.common.io.AbstractByteBuffer;

public interface Codec<T> {
    T read(AbstractByteBuffer buffer, int version);

    void write(T val, AbstractByteBuffer buffer);

    int getVersion();

    int getMagicNumber();
}
