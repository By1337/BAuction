package org.by1337.bauction.serialize;

import java.io.IOException;

public class FileDeleteException extends IOException {
    public FileDeleteException() {
    }

    public FileDeleteException(String message) {
        super(message);
    }

    public FileDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDeleteException(Throwable cause) {
        super(cause);
    }
}
