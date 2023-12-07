package org.by1337.bauction.serialize;

import java.io.IOException;

public class FileCreateException extends IOException {
    public FileCreateException() {
    }

    public FileCreateException(String message) {
        super(message);
    }

    public FileCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileCreateException(Throwable cause) {
        super(cause);
    }
}
