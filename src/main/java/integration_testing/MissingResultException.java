package integration_testing;

/** Thrown when no result was saved for the requested instance. */
public class MissingResultException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message what was missing and where.
     * @param cause   the underlying file-system error.
     */
    public MissingResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
