package io.crunch.media.shared;

public class MediaFileServerException extends RuntimeException {

    /**
     * Constructs a new <code>MediaFileServerException</code> exception
     * with <code>null</code> as its detail message.
     */
    public MediaFileServerException() {
        super();
    }

    /**
     * Constructs a new <code>MediaFileServerException</code> exception
     * with the specified detail message.
     *
     * @param message the detail message.
     */
    public MediaFileServerException(String message) {
        super(message);
    }

    /**
     * Constructs a new <code>MediaFileServerException</code> exception
     * with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause.
     */
    public MediaFileServerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new <code>MediaFileServerException</code> exception
     * with the specified cause.
     *
     * @param cause the cause.
     */
    public MediaFileServerException(Throwable cause) {
        super(cause);
    }
}
