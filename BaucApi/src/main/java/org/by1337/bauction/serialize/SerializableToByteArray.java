package org.by1337.bauction.serialize;

import java.io.IOException;

public interface SerializableToByteArray {
    byte[] getBytes() throws IOException;
}
