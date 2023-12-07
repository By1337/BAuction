package org.by1337.bauction.serialize;

import java.io.IOException;

@FunctionalInterface
public interface Deserializable<T> {
    T deserialize(byte[] arr) throws IOException;
}
