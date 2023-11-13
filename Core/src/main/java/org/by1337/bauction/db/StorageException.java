package org.by1337.bauction.db;

public class StorageException extends Exception {
    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }


    public StorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static class LostItemException extends StorageException {
        public LostItemException() {
        }

        public LostItemException(String s) {
            super(s);
        }

        public LostItemException(String message, Throwable cause) {
            super(message, cause);
        }

        public LostItemException(Throwable cause) {
            super(cause);
        }
    }

    public static class LostItemOwner extends StorageException {
        public LostItemOwner() {
        }

        public LostItemOwner(String s) {
            super(s);
        }

        public LostItemOwner(String message, Throwable cause) {
            super(message, cause);
        }

        public LostItemOwner(Throwable cause) {
            super(cause);
        }
    }

    public static class NotFoundException extends StorageException {
        public NotFoundException() {
        }

        public NotFoundException(String message) {
            super(message);
        }

        public NotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public NotFoundException(Throwable cause) {
            super(cause);
        }

        public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
