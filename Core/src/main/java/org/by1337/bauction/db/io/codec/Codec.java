package org.by1337.bauction.db.io.codec;

import org.by1337.blib.io.ByteBuffer;

public interface Codec<T> {
    T read(ByteBuffer buffer, int version);
    void write(T val, ByteBuffer buffer);
}
