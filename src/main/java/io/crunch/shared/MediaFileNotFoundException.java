package io.crunch.shared;

public class MediaFileNotFoundException extends RuntimeException {

    /**
     * Constructs a new <code>MediaFileNotFoundException</code> exception
     * with <code>null</code> as its detail message.
     */
    public MediaFileNotFoundException() {
        super();
    }

    /**
     * Constructs a new <code>MediaFileNotFoundException</code> exception
     * with the specified detail message.
     *
     * @param message the detail message.
     */
    public MediaFileNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new <code>MediaFileNotFoundException</code> exception
     * with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause.
     */
    public MediaFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new <code>MediaFileNotFoundException</code> exception
     * with the specified cause.
     *
     * @param cause the cause.
     */
    public MediaFileNotFoundException(Throwable cause) {
        super(cause);
    }
}
